package com.example.gym.client;

import com.example.gym.dto.request.TrainerWorkloadRequest;
import com.example.gym.exception.TrainingAggregatorUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TrainerWorkloadClient
{
    private final RestClient client;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public TrainerWorkloadClient(RestClient.Builder clientBuilder,
                                 CircuitBreakerFactory<?, ?> circuitBreakerFactory)
    {
        client = clientBuilder.build();
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public void updateWorkload(TrainerWorkloadRequest request)
    {
        String transactionId = MDC.get("transactionId");

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        circuitBreakerFactory.create("trainingAggregator")
                        .run(() -> {
                            client.post()
                                    .uri("http://training-aggregator/api/workloads")
                                    .headers(headers->
                                    {
                                        if (transactionId != null)
                                        {
                                            headers.add(
                                                    "X-Transaction-Id",
                                                    transactionId
                                            );
                                        }

                                        if (authentication instanceof
                                                JwtAuthenticationToken jwtAuthentication)
                                        {
                                            String token = jwtAuthentication.getToken().getTokenValue();
                                            headers.setBearerAuth(token);
                                        }
                                    })
                                    .body(request)
                                    .retrieve()
                                    .toBodilessEntity();

                            return null;
                        }, throwable -> {
                            throw new TrainingAggregatorUnavailableException(
                                    "Training aggregator is unavailable",
                                    throwable
                            );
                        });
    }
}
