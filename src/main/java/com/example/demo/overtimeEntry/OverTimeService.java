package com.example.demo.overtimeEntry;

import com.example.demo.common.enums.SettlementStatus;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.overtimeEntry.dto.OvertimeDayBreakdownDto;
import com.example.demo.overtimeEntry.dto.OvertimeSummaryResponse;
import com.example.demo.overtimeEntry.dto.SettlementResponse;
import com.example.demo.overtimeEntry.event.OvertimeSettledEvent;
import com.example.demo.worker.Worker;
import com.example.demo.worker.WorkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class OverTimeService {
    @Autowired
    private OverTimeRepository overTimeRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public OvertimeSummaryResponse getMonthlySummary(Long workerId, YearMonth month) {

        Worker worker = workerRepository.findById(workerId).orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        LocalDate startDate = month.atDay(1);

        LocalDate endDate = month.atEndOfMonth();

        List<OvertimeEntry> entries = overTimeRepository.findByWorkerIdAndDateBetween(workerId, startDate, endDate);

        BigDecimal totalHours = entries.stream().map(OvertimeEntry::getOvertimeHours).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = entries.stream().map(OvertimeEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OvertimeDayBreakdownDto> breakdown = entries.stream().map(entry -> OvertimeDayBreakdownDto.builder().date(entry.getDate()).overtimeHours(entry.getOvertimeHours()).amount(entry.getAmount()).settlementStatus(entry.getSettlementStatus().name()).build()).toList();

        String overallStatus = entries.stream().allMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED) ? "SETTLED" : "PENDING";

        return OvertimeSummaryResponse.builder().workerId(worker.getId()).workerName(worker.getName()).totalOvertimeHours(totalHours).totalPayoutAmount(totalAmount).settlementStatus(overallStatus).breakdown(breakdown).build();
    }

    @Transactional
    public SettlementResponse settleMonthlyOvertime(Long workerId, YearMonth month) {

        if (month.equals(YearMonth.now())) {

            throw new BusinessValidationException("CURRENT_MONTH_SETTLEMENT", "Cannot settle current month");
        }

        LocalDate startDate = month.atDay(1);

        LocalDate endDate = month.atEndOfMonth();

        List<OvertimeEntry> entries = overTimeRepository.findEntriesForSettlement(workerId, startDate, endDate);

        if (entries.isEmpty()) {

            throw new BusinessValidationException("NO_OVERTIME_ENTRIES", "No overtime entries found");
        }

        boolean allSettled = entries.stream().allMatch(entry -> entry.getSettlementStatus() == SettlementStatus.SETTLED);

        if (allSettled) {

            throw new BusinessValidationException("ALREADY_SETTLED", "Overtime for this month is already settled");
        }


        BigDecimal totalAmount = BigDecimal.ZERO;

        int settledCount = 0;

        for (OvertimeEntry entry : entries) {

            if (entry.getSettlementStatus() == SettlementStatus.SETTLED) {
                continue;
            }

            entry.setSettlementStatus(SettlementStatus.SETTLED);

            totalAmount = totalAmount.add(entry.getAmount());

            settledCount++;
        }

        overTimeRepository.saveAll(entries);

        eventPublisher.publishEvent(new OvertimeSettledEvent(workerId, month.toString(), totalAmount));

        return SettlementResponse.builder().workerId(workerId).month(month.toString()).totalSettledAmount(totalAmount).entriesSettled(settledCount).build();
    }

}
