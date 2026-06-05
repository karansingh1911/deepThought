package com.example.demo.overtimeEntry;

import com.example.demo.common.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OverTimeRepository extends JpaRepository<OvertimeEntry, Long> {
    @Query("""
            SELECT COALESCE(SUM(o.overtimeHours),0)
            FROM OvertimeEntry o
            WHERE o.worker.id = :workerId
            AND YEAR(o.date) = :year
            AND MONTH(o.date) = :month
            """)
    BigDecimal getMonthlyOvertimeHours(@Param("workerId") Long workerId, @Param("year") Integer year, @Param("month") Integer month);

    List<OvertimeEntry> findByWorkerIdAndDateBetween(Long workerId, LocalDate startDate, LocalDate endDate);

    boolean existsByWorkerIdAndDateBetweenAndSettlementStatus(Long workerId, LocalDate startDate, LocalDate endDate, SettlementStatus settlementStatus);


    @Query("""
            SELECT o0
            FROM OvertimeEntry o
            WHERE o.worker.id = :workerId
            AND o.date BETWEEN :startDate AND :endDate
            """)
    List<OvertimeEntry> findEntriesForSettlement(@Param("workerId") Long workerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


}
