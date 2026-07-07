package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByName(String name);

    List<Item> findByNameIgnoreCase(String name);

    boolean existsByName(String name);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Item> findByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT i.id, i.name, i.sellTo, i.price FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Object[]> findBasicInfoByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT i.id, i.name, i.sellTo, i.price FROM Item i WHERE LOWER(i.name) = LOWER(:name)")
    List<Object[]> findBasicInfoByNameExactIgnoreCase(@Param("name") String name);

    @Query("SELECT i FROM Item i WHERE LOWER(i.sellTo) = LOWER(:sellTo)")
    Page<Item> findBySellTo(@Param("sellTo") String sellTo, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')) AND LOWER(i.sellTo) = LOWER(:sellTo)")
    Page<Item> findByNameContainingAndSellTo(@Param("name") String name, @Param("sellTo") String sellTo,
            Pageable pageable);

    @Query("SELECT i FROM Item i JOIN i.droppedBy d WHERE LOWER(d) LIKE LOWER(CONCAT('%', :creature, '%'))")
    List<Item> findByDroppedByContaining(@Param("creature") String creature);

    // Queries para itens do Tibia Draptor
    Optional<Item> findByTibiadraptorItemId(Long tibiadraptorItemId);

    @Query("SELECT i FROM Item i WHERE i.isWeeklyTask = true")
    List<Item> findWeeklyTaskItems();

    @Query("SELECT i FROM Item i WHERE i.isWeeklyTask = true")
    Page<Item> findWeeklyTaskItemsPageable(Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.isWeeklyTask = false")
    List<Item> findTibiaDraptorItems();

    @Query("SELECT i FROM Item i WHERE i.isWeeklyTask = true AND LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Item> findWeeklyTaskItemsByNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.isWeeklyTask = false AND LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Item> findTibiaDraptorItemsByNameContaining(@Param("name") String name, Pageable pageable);
}
