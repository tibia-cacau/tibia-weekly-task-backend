package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.Monster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonsterRepository extends JpaRepository<Monster, Long> {

    Optional<Monster> findByNameIgnoreCase(String name);

    Optional<Monster> findByTibiadraptorId(Long tibiadraptorId);

    @Query("SELECT m FROM Monster m WHERE LOWER(m.areas) LIKE LOWER(CONCAT('%', :area, '%'))")
    List<Monster> findByAreasContaining(@Param("area") String area);

    @Query("SELECT m FROM Monster m WHERE m.hitpoints >= :minHp AND m.hitpoints <= :maxHp")
    List<Monster> findByHitpointsRange(@Param("minHp") Integer minHp, @Param("maxHp") Integer maxHp);

    @Query("SELECT m FROM Monster m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Monster> searchByName(@Param("searchTerm") String searchTerm);

    @Query("SELECT DISTINCT m FROM Monster m LEFT JOIN FETCH m.resistances WHERE LOWER(m.name) IN :names")
    List<Monster> findByNameInIgnoreCase(@Param("names") List<String> names);

    boolean existsByTibiadraptorId(Long tibiadraptorId);
}
