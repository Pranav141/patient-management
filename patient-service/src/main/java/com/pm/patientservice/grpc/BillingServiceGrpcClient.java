package com.pm.patientservice.grpc;

import billing.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class BillingServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private  BillingServiceGrpc.BillingServiceBlockingStub blockingStub;
    private final LoadBalancerClient loadBalancerClient;

    public BillingServiceGrpcClient(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }


//  used to initialize a connection between billing service grpc and patientservice.
    @PostConstruct
    public void init(){
        ServiceInstance instance = loadBalancerClient.choose("billing-service");
        if(instance == null){
            log.warn("No Instance of billing-service found");
            throw new RuntimeException("No Instance of billing-service found");
        }
        String grpcPortStr = instance.getMetadata().get("grpc-port");
        System.out.println(instance.getHost()+instance.getMetadata().get("grpc-port"));
        int grpcPort = grpcPortStr != null ? Integer.parseInt(grpcPortStr) : instance.getPort();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(instance.getHost(),grpcPort)
                .usePlaintext()
                .build();

        this.blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String patientId, String name, String email)
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
