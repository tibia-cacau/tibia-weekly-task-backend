package com.tibia.weeklytasks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibia.weeklytasks.dto.tibiadraptor.BestiaryResponse;
import com.tibia.weeklytasks.dto.tibiadraptor.MonsterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TibiaDraptorClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;

    public TibiaDraptorClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${tibiadraptor.api.url:https://tibiadraptor.com/api/v1}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiBaseUrl = apiBaseUrl;
    }

    public BestiaryResponse fetchBestiaryPage(int page) {
        try {
            // Build URL with query parameters
            String url = String.format("%s/bestiary?page=%d", apiBaseUrl, page);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<BestiaryResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    BestiaryResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched page {} from Tibia Draptor API", page);
                BestiaryResponse body = response.getBody();
                log.info("Response body - data: {}, meta: {}",
                        body.getData() != null ? body.getData().size() : "null",
                        body.getMeta());
                return body;
            } else {
                log.error("Failed to fetch page {} from Tibia Draptor API. Status: {}", page, response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("Error fetching bestiary page {}: {}", page, e.getMessage(), e);
            return null;
        }
    }

    public List<MonsterDto> fetchAllMonsters() {
        List<MonsterDto> allMonsters = new ArrayList<>();

        log.info("Starting to fetch all monsters from Tibia Draptor API");

        // Fetch first page to get total pages count
        BestiaryResponse firstPage = fetchBestiaryPage(1);
        if (firstPage == null || firstPage.getData() == null) {
            log.error("Failed to fetch first page of bestiary");
            return allMonsters;
        }

        allMonsters.addAll(firstPage.getData());

        int totalPages = firstPage.getMeta() != null ? firstPage.getMeta().getLastPage() : 14;

        log.info("Total pages to fetch: {}", totalPages);

        // Fetch remaining pages
        for (int page = 2; page <= totalPages; page++) {
            BestiaryResponse pageResponse = fetchBestiaryPage(page);
            if (pageResponse != null && pageResponse.getData() != null) {
                allMonsters.addAll(pageResponse.getData());
                log.info("Fetched page {}/{} - Total monsters so far: {}", page, totalPages, allMonsters.size());
            } else {
                log.warn("Failed to fetch page {}, skipping", page);
            }

            // Add small delay to avoid overwhelming the API
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sleep interrupted while fetching monsters");
            }
        }

        log.info("Finished fetching all monsters. Total count: {}", allMonsters.size());
        return allMonsters;
    }
}
