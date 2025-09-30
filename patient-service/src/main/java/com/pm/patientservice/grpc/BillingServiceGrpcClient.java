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
import java.util.concurrent.TimeUnit;

@Service
public class BillingServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BillingServiceGrpcClient.class);
    private  BillingServiceGrpc.BillingServiceBlockingStub blockingStub;
    private ManagedChannel channel;
    private final LoadBalancerClient loadBalancerClient;

    public BillingServiceGrpcClient(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }


//    used to initialize a connection between billing service grpc and patientservice
//    as soon as the bean for BillingServiceGrpcClient is created.
//    @PostConstruct
//    public void init(){
//        ServiceInstance instance = loadBalancerClient.choose("billing-service");
//        if(instance == null){
//            log.warn("No Instance of billing-service found");
//            throw new RuntimeException("No Instance of billing-service found");
//        }
//        String grpcPortStr = instance.getMetadata().get("grpc-port");
//        System.out.println(instance.getHost()+instance.getMetadata().get("grpc-port"));
//        int grpcPort = grpcPortStr != null ? Integer.parseInt(grpcPortStr) : instance.getPort();
//        ManagedChannel channel = ManagedChannelBuilder.forAddress(instance.getHost(),grpcPort)
//                .usePlaintext()
//                .build();
//
//        this.blockingStub = BillingServiceGrpc.newBlockingStub(channel);
//    }

    //Initialize blocking stub when it is requested
    //use singleton pattern to initialize the blocking stub

    // trying to get a stub if it already exists if not then resetConn and then create a new one.
    private BillingServiceGrpc.BillingServiceBlockingStub getStubWithRefresh(){
        try {
            return getOrCreateStub();
        } catch (Exception e) {
            log.warn("gRPC call failed, refreshing connection: {}", e.getMessage());
            resetConnection();
            return getOrCreateStub();
        }
    }

    //Actual initialization of the blockingStub
    private BillingServiceGrpc.BillingServiceBlockingStub getOrCreateStub(){
        if(this.blockingStub == null){
            //creating the connection in a thread-safe environment to avoid an abnormal behaviours
            synchronized (this){
                log.info("Creating a connection");
                createConnection();
            }
        }
        return blockingStub;
    }

    //
    private void resetConnection(){
        log.info("Resetting gRPC connection...");
        synchronized (this){
            if(channel != null && !channel.isShutdown()){
                channel.shutdown();
                channel = null;
            }
            blockingStub = null;
        }

    }

    private void createConnection(){
        ServiceInstance instance = loadBalancerClient.choose("billing-service");
        if(instance ==  null){
            throw new RuntimeException("Cannot establish a connection to billing-service");
        }

        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            channel = null;
        }

        String grpcUrl = instance.getHost();
        String grpcPortStr = instance.getMetadata().get("grpc-port");
        int grpcPort = Integer.parseInt(grpcPortStr);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcUrl,grpcPort)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(5,TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();

        this.blockingStub = BillingServiceGrpc.newBlockingStub(channel);
        log.info("Connected established to billing-service at {}:{}", grpcUrl,grpcPortStr);

    }

    //grpc calls
    public BillingResponse createBillingAccount(String patientId, String name, String email)
    {

        BillingRequest billingRequest = BillingRequest.newBuilder()
                .setEmail(email)
                .setName(name)
                .setPatientId(patientId)
                .build();
        BillingResponse response = getStubWithRefresh().createBillingAccount(billingRequest);
        log.info("Received response from billing service via GRPC: {}",response);
        return response;
    }

    public PatientBillingResponse generateBillForPatient(String patientId,float amount){
        PatientBillingRequest request = PatientBillingRequest.newBuilder()
                .setPatientId(patientId)
                .setAmount(amount)
                .build();
        PatientBillingResponse response = getStubWithRefresh().generateBillForPatient(request);
        log.info("hello world");
        return response;
    }

}
