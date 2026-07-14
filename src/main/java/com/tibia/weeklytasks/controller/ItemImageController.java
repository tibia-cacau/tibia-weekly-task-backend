package com.tibia.weeklytasks.controller;

import com.tibia.weeklytasks.model.Item;
import com.tibia.weeklytasks.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemImageController {

    private final ItemRepository itemRepository;

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getItemImage(@PathVariable Long id) {
        log.info("GET /api/items/{}/image - Fetching item image", id);

        Optional<Item> itemOpt = itemRepository.findById(id);

        if (itemOpt.isEmpty() || itemOpt.get().getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        Item item = itemOpt.get();

        try {
            byte[] imageBytes = Base64.getDecoder().decode(item.getImageData());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    item.getImageContentType() != null ? item.getImageContentType() : "image/png"));
            headers.setContentLength(imageBytes.length);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error decoding image for item {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/name/{name}/image")
    public ResponseEntity<byte[]> getItemImageByName(@PathVariable String name) {
        log.info("GET /api/items/name/{}/image - Fetching item image by name", name);

        Optional<Item> itemOpt = itemRepository.findByName(name);

        if (itemOpt.isEmpty() || itemOpt.get().getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        Item item = itemOpt.get();

        try {
            byte[] imageBytes = Base64.getDecoder().decode(item.getImageData());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    item.getImageContentType() != null ? item.getImageContentType() : "image/png"));
            headers.setContentLength(imageBytes.length);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error decoding image for item {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
