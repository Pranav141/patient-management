package com.pm.patientservice.service;

import billing.BillingResponse;
import billing.PatientBillingResponse;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.BillingAccountCreationException;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepo patientRepo;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepo patientRepo, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepo = patientRepo;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }


    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepo.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();

    }

    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepo.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExistsException("A Patient with this eamil already exist "+ patientRequestDTO.getEmail());
        }
        Patient patient = patientRepo.save(PatientMapper.toModel(patientRequestDTO));
        kafkaProducer.sendEvent(patient);
        try{
            BillingResponse response = billingServiceGrpcClient.createBillingAccount(patient.getId().toString(),patient.getName(), patient.getEmail());
            log.info("Billing Response : {}",response.toString());
        }catch (RuntimeException e){
            log.warn("Rolling back the transaction for patient with email : {}",patientRequestDTO.getEmail());
            throw new BillingAccountCreationException("Failed to create billing account for patient: " + patientRequestDTO.getEmail());
        }
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

