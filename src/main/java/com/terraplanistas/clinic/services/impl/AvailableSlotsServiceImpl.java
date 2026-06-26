package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.config.ClinicTimezoneHolder;
import com.terraplanistas.clinic.domain.dto.request.AvailableSlotsQuery;
import com.terraplanistas.clinic.domain.dto.response.AvailableSlotResponse;
import com.terraplanistas.clinic.domain.entities.Appointment;
import com.terraplanistas.clinic.domain.entities.DoctorAvailability;
import com.terraplanistas.clinic.domain.enums.AppointmentStatus;
import com.terraplanistas.clinic.repositories.AppointmentRepository;
import com.terraplanistas.clinic.repositories.DoctorAvailabilityRepository;
import com.terraplanistas.clinic.services.AvailableSlotsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailableSlotsServiceImpl implements AvailableSlotsService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    private static final Set<AppointmentStatus> BLOCKED_STATUSES = Set.of(
        AppointmentStatus.SCHEDULED, AppointmentStatus.IN_PROGRESS, AppointmentStatus.PENDING_PAYMENT
    );

    @Override
    public List<AvailableSlotResponse> findAvailableSlots(AvailableSlotsQuery query) {
        ZoneId tz = query.timezone() != null ? query.timezone() : ClinicTimezoneHolder.getClinicZone();

        List<DoctorAvailability> availabilities = availabilityRepository
            .findBySpecialtyCodeWithEmployeeAndSpecialty(query.specialtyCode()).stream()
            .filter(a -> a.getEmployeeSpecialty().getEmployeeId().equals(query.doctorId()))
            .toList();

        if (availabilities.isEmpty()) {
            return List.of();
        }

        DoctorAvailability first = availabilities.get(0);
        String doctorFirstName = first.getEmployeeSpecialty().getEmployee().getFirstName();
        String doctorLastName = first.getEmployeeSpecialty().getEmployee().getLastName();
        String specialtyName = first.getEmployeeSpecialty().getSpecialty().getName();
        int durationMinutes = first.getEmployeeSpecialty().getConsultDurationMinutes();

        LocalDate startDate = query.startDate();
        LocalDate endDate = query.endDate();

        OffsetDateTime rangeStart = startDate.atStartOfDay(tz).toOffsetDateTime();
        OffsetDateTime rangeEnd = endDate.plusDays(1).atStartOfDay(tz).toOffsetDateTime();

        List<Appointment> blockedAppointments = appointmentRepository
            .findByEmployeeIdAndStatusIn(query.doctorId(), new ArrayList<>(BLOCKED_STATUSES)).stream()
            .filter(a -> {
                OffsetDateTime expected = a.getExpectedAt();
                return !expected.isBefore(rangeStart) && !expected.isBefore(rangeStart) && expected.isBefore(rangeEnd);
            })
            .toList();

        List<AvailableSlotResponse> slots = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            List<DoctorAvailability> dayAvailabilities = availabilities.stream()
                .filter(a -> a.getDayOfWeek() == dayOfWeek)
                .toList();

            for (DoctorAvailability availability : dayAvailabilities) {
                OffsetTime startTime = availability.getStartTime();
                OffsetTime endTime = availability.getEndTime();
                LocalTime localStart = startTime.toLocalTime();
                LocalTime localEnd = endTime.toLocalTime();

                OffsetDateTime windowStart = date.atTime(localStart).atZone(tz).toOffsetDateTime();
                OffsetDateTime windowEnd = date.atTime(localEnd).atZone(tz).toOffsetDateTime();

                List<TimeSlot> freeWindows = calculateFreeWindows(windowStart, windowEnd, blockedAppointments, tz, durationMinutes);

                for (TimeSlot window : freeWindows) {
                    List<AvailableSlotResponse> stackedSlots = stackIntoSlots(
                        window, query, tz, doctorFirstName, doctorLastName, specialtyName,
                        availability.getEmployeeSpecialty().getFeePerHour(),
                        durationMinutes
                    );
                    slots.addAll(stackedSlots);
                }
            }
        }

        return slots;
    }

    private List<TimeSlot> calculateFreeWindows(OffsetDateTime windowStart, OffsetDateTime windowEnd,
            List<Appointment> blockedAppointments, ZoneId tz, int durationMinutes) {
        List<TimeSlot> freeWindows = new ArrayList<>();
        OffsetDateTime current = windowStart;

        List<Appointment> overlapping = blockedAppointments.stream()
            .filter(a -> {
                OffsetDateTime expected = a.getExpectedAt();
                return !expected.isBefore(windowStart) && expected.isBefore(windowEnd);
            })
            .sorted((a, b) -> a.getExpectedAt().compareTo(b.getExpectedAt()))
            .toList();

        for (Appointment appointment : overlapping) {
            OffsetDateTime busyStart = appointment.getExpectedAt();
            OffsetDateTime busyEnd = busyStart.plusMinutes(durationMinutes);

            if (current.isBefore(busyStart)) {
                freeWindows.add(new TimeSlot(current, busyStart));
            }
            current = busyEnd.isAfter(current) ? busyEnd : current;
        }

        if (current.isBefore(windowEnd)) {
            freeWindows.add(new TimeSlot(current, windowEnd));
        }

        return freeWindows;
    }

    private List<AvailableSlotResponse> stackIntoSlots(TimeSlot window, AvailableSlotsQuery query, ZoneId tz,
            String doctorFirstName, String doctorLastName, String specialtyName,
            java.math.BigDecimal feePerHour, int durationMinutes) {
        List<AvailableSlotResponse> slots = new ArrayList<>();

        OffsetDateTime current = window.start;
        while (current.plusMinutes(durationMinutes).isBefore(window.end) ||
               current.plusMinutes(durationMinutes).isEqual(window.end)) {
            OffsetDateTime slotEnd = current.plusMinutes(durationMinutes);

            slots.add(new AvailableSlotResponse(
                query.doctorId(),
                doctorFirstName,
                doctorLastName,
                query.specialtyCode(),
                specialtyName,
                current.toLocalDate(),
                current.toOffsetTime(),
                slotEnd.toOffsetTime(),
                feePerHour,
                durationMinutes
            ));

            current = slotEnd;
        }

        return slots;
    }

    private record TimeSlot(OffsetDateTime start, OffsetDateTime end) {}
}
