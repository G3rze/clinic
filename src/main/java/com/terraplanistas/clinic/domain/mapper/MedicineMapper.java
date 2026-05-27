package com.terraplanistas.clinic.domain.mapper;

import com.terraplanistas.clinic.domain.dto.request.MedicineRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicineResponse;
import com.terraplanistas.clinic.domain.entities.Medicine;
import com.terraplanistas.clinic.domain.entities.Laboratory;

public class MedicineMapper {

    public static Medicine toEntity(MedicineRequest request, Laboratory laboratory) {
        Medicine medicine = new Medicine();
        medicine.setLaboratory(laboratory);
        medicine.setBrandName(request.brandName());
        medicine.setGenericName(request.genericName());
        medicine.setComposition(request.composition());
        medicine.setUseFrom(request.useFrom());
        medicine.setAtcCode(request.atcCode());
        return medicine;
    }

    public static Medicine toUpgrade(MedicineRequest request, Medicine medicine) {
        medicine.setBrandName(request.brandName());
        medicine.setGenericName(request.genericName());
        medicine.setComposition(request.composition());
        medicine.setUseFrom(request.useFrom());
        medicine.setAtcCode(request.atcCode());
        return medicine;
    }

    public static MedicineResponse toResponse(Medicine medicine) {
        return new MedicineResponse(
            medicine.getId(),
            medicine.getLaboratory() != null ? medicine.getLaboratory().getId() : null,
            medicine.getBrandName(),
            medicine.getGenericName(),
            medicine.getComposition(),
            medicine.getUseFrom(),
            medicine.getAtcCode()
        );
    }
}