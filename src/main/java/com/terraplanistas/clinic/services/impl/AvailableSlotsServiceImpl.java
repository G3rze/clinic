package com.terraplanistas.clinic.services.impl;

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
        AppointmentStatus.SCHEDULED, AppointmentStatus.IN_PROGRESS
    );

    @Override
    public List<AvailableSlotResponse> findAvailableSlots(AvailableSlotsQuery query) {
        ZoneId tz = query.timezone() != null ? query.timezone() : ZoneId.systemDefault();
        int durationMinutes = query.consultDurationMinutes();

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
                OffsetTime localStart = availability.getStartTime();
                OffsetTime localEnd = availability.getEndTime();

                OffsetDateTime windowStart = date.atTime(localStart).atZoneSameInstant(tz).toOffsetDateTime();
                OffsetDateTime windowEnd = date.atTime(localEnd).atZoneSameInstant(tz).toOffsetDateTime();

                List<TimeSlot> freeWindows = calculateFreeWindows(windowStart, windowEnd, blockedAppointments, tz, durationMinutes);

                for (TimeSlot window : freeWindows) {
                    List<AvailableSlotResponse> stackedSlots = stackIntoSlots(window, query, tz, doctorFirstName, doctorLastName, specialtyName);
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
            String doctorFirstName, String doctorLastName, String specialtyName) {
        List<AvailableSlotResponse> slots = new ArrayList<>();
        int duration = query.consultDurationMinutes();

        OffsetDateTime current = window.start;
        while (current.plusMinutes(duration).isBefore(window.end) ||
               current.plusMinutes(duration).isEqual(window.end)) {
            OffsetDateTime slotEnd = current.plusMinutes(duration);

            slots.add(new AvailableSlotResponse(
                query.doctorId(),
                doctorFirstName,
                doctorLastName,
                query.specialtyCode(),
                specialtyName,
                current.toLocalDate(),
                current.toOffsetTime(),
                slotEnd.toOffsetTime()
            ));

            current = slotEnd;
        }

        return slots;
    }

    private record TimeSlot(OffsetDateTime start, OffsetDateTime end) {}
}
