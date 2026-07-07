package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.dto.TibiaCoinPriceRequest;
import com.tibia.weeklytasks.dto.TibiaCoinPriceResponse;
import com.tibia.weeklytasks.service.TibiaCoinService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tibia-coins")
@CrossOrigin(origins = "*")
public class TibiaCoinController {

    private final TibiaCoinService tibiaCoinService;

    public TibiaCoinController(TibiaCoinService tibiaCoinService) {
        this.tibiaCoinService = tibiaCoinService;
    }

    @GetMapping("/price")
    public ResponseEntity<TibiaCoinPriceResponse> getPrice(@RequestParam Integer quantity) {
        TibiaCoinPriceResponse response = tibiaCoinService.getTibiaCoinPrice(quantity);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/price")
    public ResponseEntity<TibiaCoinPriceResponse> getPricePost(@RequestBody TibiaCoinPriceRequest request) {
        TibiaCoinPriceResponse response = tibiaCoinService.getTibiaCoinPrice(request.getQuantity());
        return ResponseEntity.ok(response);
    }
}
