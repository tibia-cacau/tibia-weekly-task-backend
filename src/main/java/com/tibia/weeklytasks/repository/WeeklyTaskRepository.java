package com.tibia.weeklytasks.repository;

import com.tibia.weeklytasks.model.WeeklyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyTaskRepository extends JpaRepository<WeeklyTask, Long> {

    List<WeeklyTask> findByTaskType(String taskType);

    List<WeeklyTask> findByDifficultyLessThanEqual(Integer difficulty);

    List<WeeklyTask> findByMonsterName(String monsterName);

    List<WeeklyTask> findByItemName(String itemName);
}
