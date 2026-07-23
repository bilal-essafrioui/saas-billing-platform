package com.saas.billing.subscription_service.client;



import com.saas.billing.subscription_service.dto.request.CreatePaymentIntentRequest;
import com.saas.billing.subscription_service.dto.response.ChargePaymentResponse;
import com.saas.billing.subscription_service.dto.response.CreatePaymentIntentResponse;
import com.saas.billing.subscription_service.dto.request.ChargePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final RestClient restClient;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    public CreatePaymentIntentResponse createPaymentIntent(
            CreatePaymentIntentRequest request,
            String userId,
            String userEmail
    ) {

        return restClient.post()
                .uri(paymentServiceUrl + "/internal/payments/create-intent")
                .header("X-User-Id", userId)
                .header("X-User-Email", userEmail)
                .body(request)
                .retrieve()
                .body(CreatePaymentIntentResponse.class);
    }

    public ChargePaymentResponse chargePayment(
            ChargePaymentRequest request
    ) {

        System.out.println("5. PaymentClient called");

        return restClient.post()
                .uri(paymentServiceUrl + "/internal/payments/charge")
                .body(request)
                .retrieve()
                .body(ChargePaymentResponse.class);
    }
}
