package com.sbadss.repository;

import com.sbadss.entity.PredictionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PredictionRecordRepository extends JpaRepository<PredictionRecord, Long> {
    List<PredictionRecord> findByBranchIdAndPredictionDateGreaterThanEqual(Long branchId, LocalDate date);
}
