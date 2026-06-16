package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.AvailableSlotsQuery;
import com.terraplanistas.clinic.domain.dto.response.AvailableSlotResponse;
import java.util.List;

public interface AvailableSlotsService {

    List<AvailableSlotResponse> findAvailableSlots(AvailableSlotsQuery query);
}
