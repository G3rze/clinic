package com.terraplanistas.clinic.services.impl;

import com.terraplanistas.clinic.domain.dto.request.MedicineRequest;
import com.terraplanistas.clinic.domain.dto.response.MedicineResponse;
import com.terraplanistas.clinic.domain.entities.Laboratory;
import com.terraplanistas.clinic.domain.entities.Medicine;
import com.terraplanistas.clinic.domain.mapper.MedicineMapper;
import com.terraplanistas.clinic.repositories.LaboratoryRepository;
import com.terraplanistas.clinic.repositories.MedicineRepository;
import com.terraplanistas.clinic.services.MedicineService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final LaboratoryRepository laboratoryRepository;

    public MedicineServiceImpl(MedicineRepository medicineRepository, LaboratoryRepository laboratoryRepository) {
        this.medicineRepository = medicineRepository;
        this.laboratoryRepository = laboratoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll().stream()
                .map(MedicineMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getMedicineById(UUID id) {
        return medicineRepository.findById(id)
                .map(MedicineMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> searchMedicines(String search, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("brandName").ascending());
        Page<Medicine> medicinePage;

        if (search == null || search.trim().isEmpty()) {
            medicinePage = medicineRepository.findAll(pageRequest);
        } else {
            medicinePage = medicineRepository.searchByAnyField(search.trim(), pageRequest);
        }

        return medicinePage.getContent().stream()
                .map(MedicineMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MedicineResponse createMedicine(MedicineRequest request, UUID currentUserId) {
        Laboratory laboratory = laboratoryRepository.findById(request.laboratoryId())
                .orElseThrow(() -> new EntityNotFoundException("Laboratory not found: " + request.laboratoryId()));

        Medicine medicine = MedicineMapper.toEntity(request, laboratory);
        medicine.setCreatedBy(currentUserId);
        medicine.setUpdatedBy(currentUserId);

        return MedicineMapper.toResponse(medicineRepository.save(medicine));
    }

    @Override
    @Transactional
    public MedicineResponse updateMedicine(UUID id, MedicineRequest request, UUID currentUserId) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + id));

        Laboratory laboratory = laboratoryRepository.findById(request.laboratoryId())
                .orElseThrow(() -> new EntityNotFoundException("Laboratory not found: " + request.laboratoryId()));

        medicine = MedicineMapper.toUpgrade(request, medicine, laboratory);
        medicine.setUpdatedBy(currentUserId);

        return MedicineMapper.toResponse(medicineRepository.save(medicine));
    }

    @Override
    @Transactional
    public void deleteMedicine(UUID id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found: " + id));
        medicineRepository.delete(medicine);
    }
}