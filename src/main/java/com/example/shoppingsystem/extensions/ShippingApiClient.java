package com.example.shoppingsystem.extensions;

import com.example.shoppingsystem.requests.ShippingRequest;
import com.example.shoppingsystem.responses.ShipmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
@RequiredArgsConstructor
public class ShippingApiClient {
//    private final RestTemplate restTemplate;
//
//    public ShipmentResponse sendOrder(ShippingRequest shippingRequest) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Token", "API_KEY_CUA_BEN_VAN_CHUYEN");
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<ShippingRequest> entity = new HttpEntity<>(shippingRequest, headers);
//
//        ResponseEntity<ShipmentResponse> response = restTemplate.postForEntity(
//                "https://api.deliverypartner.vn/create-order",
//                entity,
//                ShipmentResponse.class
//        );
//        return response.getBody();
//    }
}
