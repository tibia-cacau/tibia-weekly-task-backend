package com.tibia.weeklytasks.mapper;

import com.tibia.weeklytasks.dto.ItemDTO;
import com.tibia.weeklytasks.model.Item;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ItemMapper {

    public ItemDTO toDTO(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .imageUrl(item.getId() != null ? "/api/items/" + item.getId() + "/image" : null)
                .droppedBy(item.getDroppedBy())
                .sellToNpc(item.getSellTo())
                .priceAtNpc(item.getPrice())
                .build();
    }

    public Item toEntity(ItemDTO dto) {
        if (dto == null) {
            return null;
        }

        Item item = Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .droppedBy(dto.getDroppedBy())
                .sellTo(dto.getSellToNpc())
                .price(dto.getPriceAtNpc())
                .build();

        if (dto.getId() == null) {
            item.setCreatedAt(LocalDateTime.now());
        }
        item.setUpdatedAt(LocalDateTime.now());

        return item;
    }
}
