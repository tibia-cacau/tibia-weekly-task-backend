package com.tibia.weeklytasks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tibia.weeklytasks.dto.TibiaCoinPriceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class TibiaCoinService {

    private static final int MAX_QUANTITY = 10000;

    @Value("${reidoscoins.api.url:https://www.reidoscoins.com.br/index.php}")
    private String apiUrl;

    @Value("${reidoscoins.api.token:NBMRJK348THG3W7TRY2GHBV38245YR1291}")
    private String apiToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TibiaCoinService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public TibiaCoinPriceResponse getTibiaCoinPrice(Integer quantity) {
        // Validar quantidade
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        
        if (quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantidade máxima permitida é " + MAX_QUANTITY);
        }
        
        // Construir URL com parâmetros
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("route", "product/product/code_price_tibia")
                .queryParam("token", apiToken)
                .queryParam("quantity", quantity)
                .toUriString();

        try {
            // Fazer requisição GET para API do Rei dos Coins como String
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Resposta vazia da API do Rei dos Coins");
            }
            
            // Verificar se a resposta contém mensagens de erro conhecidas
            if (responseBody.contains("Quantidade máxima")) {
                throw new IllegalArgumentException("Quantidade excede o limite permitido pela API: " + responseBody);
            }
            
            // Verificar se parece ser JSON (começa com { ou [)
            String trimmedBody = responseBody.trim();
            if (!trimmedBody.startsWith("{") && !trimmedBody.startsWith("[")) {
                throw new RuntimeException("Resposta da API não é um JSON válido: " + responseBody);
            }
            
            // Tentar fazer parse do JSON
            TibiaCoinPriceResponse priceResponse = objectMapper.readValue(responseBody, TibiaCoinPriceResponse.class);
            
            // Calcular total se não vier da API
            if (priceResponse != null && priceResponse.getTotal() == null && priceResponse.getPriceNumeric() != null) {
                priceResponse.setTotal(priceResponse.getPriceNumeric() * quantity);
            }
            
            return priceResponse;
        } catch (IllegalArgumentException e) {
            // Re-lançar erros de validação
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar preço dos Tibia Coins: " + e.getMessage(), e);
        }
    }
}
