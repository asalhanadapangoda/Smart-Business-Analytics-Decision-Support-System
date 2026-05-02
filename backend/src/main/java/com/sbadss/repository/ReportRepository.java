package com.sbadss.repository;

import com.sbadss.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByBranchId(Long branchId);
    List<Report> findByGeneratedById(Long userId);
    List<Report> findByStatus(Report.ReportStatus status);
}
