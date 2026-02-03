package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.WeeklyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyTaskRepository extends JpaRepository<WeeklyTask, Long> {

    List<WeeklyTask> findByTaskType(String taskType);

    List<WeeklyTask> findByDifficultyLessThanEqual(Integer difficulty);

    List<WeeklyTask> findByMonsterName(String monsterName);

    List<WeeklyTask> findByItemName(String itemName);

    // Search by item name or monster name
    @Query("SELECT t FROM WeeklyTask t WHERE " +
            "LOWER(t.itemName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.monsterName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<WeeklyTask> searchByItemOrMonster(@Param("searchTerm") String searchTerm);

    // Find tasks by multiple monster names
    @Query("SELECT t FROM WeeklyTask t WHERE LOWER(t.monsterName) IN :monsterNames")
    List<WeeklyTask> findByMonsterNameIn(@Param("monsterNames") List<String> monsterNames);

    // Find ITEM_DELIVERY tasks where items are dropped by monsters
    @Query("SELECT DISTINCT t FROM WeeklyTask t " +
            "JOIN t.monster m " +
            "WHERE LOWER(m.name) IN :monsterNames " +
            "AND t.taskType = 'ITEM_DELIVERY'")
    List<WeeklyTask> findItemTasksByMonsterNames(@Param("monsterNames") List<String> monsterNames);
}
