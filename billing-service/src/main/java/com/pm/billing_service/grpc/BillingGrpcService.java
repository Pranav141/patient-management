package com.pm.billing_service.grpc;

import billing.BillingResponse;
import billing.BillingServiceGrpc;
import billing.PatientBillingResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(billing.BillingRequest billingRequest,StreamObserver<BillingResponse> responseStreamObserver){
        log.info("Create Account Request Recieved {}",billingRequest.toString());

        //Some Business logic

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();
        responseStreamObserver.onNext(response);
        responseStreamObserver.onCompleted();
    }

    @Override
    public void generateBillForPatient(billing.PatientBillingRequest request, StreamObserver<PatientBillingResponse> responseStreamObserver){
        log.info("Request Recieved for genearating the bill for patient id {} and amount {}",request.getPatientId(),request.getAmount());

        //Some business logic

        PatientBillingResponse response = PatientBillingResponse.newBuilder()
                .setBillNumber("1234")
                .setTotalAmount(request.getAmount()+(request.getAmount()*0.1f))
                .build();
        responseStreamObserver.onNext(response);
        responseStreamObserver.onCompleted();
    }
}
