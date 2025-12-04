package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends MongoRepository<Item, String> {

    Optional<Item> findByName(String name);

    boolean existsByName(String name);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<Item> findByNameContaining(String name, Pageable pageable);

    List<Item> findByDroppedByContaining(String creature);
}
