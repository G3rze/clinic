package com.terraplanistas.clinic.services;

import com.terraplanistas.clinic.domain.dto.request.MedicineRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicineResponse;

import java.util.List;
import java.util.UUID;

public interface MedicineService {

    List<MedicineResponse> getAllMedicines();

    MedicineResponse getMedicineById(UUID id);

    List<MedicineResponse> searchMedicines(String search, int page, int size);

    MedicineResponse createMedicine(MedicineRequest request, UUID currentUserId);

    MedicineResponse updateMedicine(UUID id, MedicineRequest request, UUID currentUserId);

    void deleteMedicine(UUID id);
}