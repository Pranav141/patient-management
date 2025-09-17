package com.pm.patientservice.service;

import billing.PatientBillingResponse;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepo patientRepo;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
//    public PatientService(PatientRepo patientRepo) {
//        this.patientRepo = patientRepo;
//    }

    public PatientService(PatientRepo patientRepo, BillingServiceGrpcClient billingServiceGrpcClient) {
        this.patientRepo = patientRepo;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepo.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();

    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepo.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("A Patient with this eamil already exist "+ patientRequestDTO.getEmail());
        }
        Patient patient = patientRepo.save(PatientMapper.toModel(patientRequestDTO));
        billingServiceGrpcClient.createBillingAccount(patient.getId().toString(),patient.getName(), patient.getEmail());
        return PatientMapper.toDTO(patient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepo.findById(id).orElseThrow(()-> new PatientNotFoundException("Patient with id:- "+ id+ " Not found"));
        if(patient.getEmail().equals(patientRequestDTO.getEmail()) || !patientRepo.existsByEmail(patientRequestDTO.getEmail())){
            Patient p = PatientMapper.toModel(patientRequestDTO);
            p.setId(id);
            patientRepo.save(p);
            return PatientMapper.toDTO(p);

        }
        else{
            throw new EmailAlreadyExistsException("A Patient with this eamil already exist "+ patientRequestDTO.getEmail());
        }
    }

    public void deletePatient(UUID id){
        patientRepo.deleteById(id);
    }

    public String generateBill(String id) {
        PatientBillingResponse response = billingServiceGrpcClient.generateBillForPatient(id,32.43f);
        return response.toString();
    }
}

