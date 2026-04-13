package com.nckh.genealogy.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// SepayController.java
@RestController
@RequestMapping("/api/v1/sepay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SepayController {

    static final String SEPAY_TOKEN = "LSMHHARWZOJRMDNFGKIER38JXLBC7QF0T1LMS6QGY5WAWGVTP1D6JEBBJUKZFX29";
    static final String SEPAY_URL = "https://my.sepay.vn/userapi/transactions/list";

    @GetMapping("/transactions")
    public ResponseEntity<String> getTransactions() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SEPAY_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SEPAY_TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return ResponseEntity.status(response.statusCode())
                .header("Content-Type", "application/json")
                .body(response.body());
    }
}