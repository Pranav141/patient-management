package com.pm.patientservice.grpc;

import billing.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}")String serverAddress,
            @Value("${billing.service.grpc.port:9001}") int serverPort
    ) {
        log.info("Connecting to Billing Service Grpc service at {}:{}",serverAddress,serverPort);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress,serverPort).usePlaintext().build();
        blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String patientId,String name,String email)
    {

        BillingRequest billingRequest = BillingRequest.newBuilder()
                .setEmail(email)
                .setName(name)
                .setPatientId(patientId)
                .build();
        BillingResponse response = blockingStub.createBillingAccount(billingRequest);
        log.info("Received response from billing service via GRPC: {}",response);
        return response;
    }

    public PatientBillingResponse generateBillForPatient(String patientId,float amount){
        PatientBillingRequest request = PatientBillingRequest.newBuilder()
                .setPatientId(patientId)
                .setAmount(amount)
                .build();
        PatientBillingResponse response = blockingStub.generateBillForPatient(request);
        log.info("hello world");
        return response;
    }

}
