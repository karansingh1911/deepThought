package com.example.demo.attendance;

import com.example.demo.attendance.dto.AttendanceResponse;
import com.example.demo.attendance.dto.CheckInRequest;
import com.example.demo.attendance.dto.CheckOutRequest;
import com.example.demo.attendance.dto.PaginatedResponse;
import com.example.demo.cache.ActiveWorkerCacheService;
import com.example.demo.cache.ActiveWorkerDto;
import com.example.demo.common.enums.SettlementStatus;
import com.example.demo.exception.BusinessValidationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.overtimeEntry.OverTimeRepository;
import com.example.demo.overtimeEntry.OvertimeEntry;
import com.example.demo.site.Site;
import com.example.demo.site.SiteRepository;
import com.example.demo.worker.Worker;
import com.example.demo.worker.WorkerRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    WorkerRepository workerRepository;
    @Autowired
    SiteRepository siteRepository;
    @Autowired
    OverTimeRepository overTimeRepository;
    @Autowired
    ActiveWorkerCacheService cacheService;

    public AttendanceResponse clockIn(@Valid CheckInRequest request) {
        Worker worker = workerRepository.findById(request.getWorkerId()).orElseThrow(() -> new ResourceNotFoundException("Worker not found with id: " + request.getWorkerId()));

        if (!worker.getActive()) {
            throw new BusinessValidationException("INACTIVE_WORKER", "Worker is inactive");
        }

        Site site = siteRepository.findById(request.getSiteId()).orElseThrow(() -> new ResourceNotFoundException("Site not found with id: " + request.getSiteId()));

        if (!site.getActive()) {
            throw new BusinessValidationException("INACTIVE_SITE", "Site is inactive");
        }

        attendanceRepository.findByWorkerIdAndClockOutIsNull(worker.getId()).ifPresent(attendance -> {
            throw new BusinessValidationException("DUPLICATE_CLOCK_IN", "Worker is already clocked in at Site: " + attendance.getSite().getSiteName());
        });

        Attendance attendance = new Attendance();

        attendance.setWorker(worker);
        attendance.setSite(site);

        attendance.setAttendanceDate(LocalDate.now());

        attendance.setClockIn(LocalDateTime.now());

        attendance.setFlagged(false);

        attendance.setTotalHoursWorked(BigDecimal.ZERO);

        attendance.setOvertimeHours(BigDecimal.ZERO);

        Attendance savedAttendance = attendanceRepository.save(attendance);

        cacheService.addWorker(ActiveWorkerDto.builder().workerId(worker.getId()).workerName(worker.getName()).siteId(site.getId()).siteName(site.getSiteName()).clockInTime(savedAttendance.getClockIn()).build());

        return AttendanceResponse.builder().attendanceId(savedAttendance.getId()).workerId(worker.getId()).workerName(worker.getName()).siteId(site.getId()).siteName(site.getSiteName()).attendanceDate(savedAttendance.getAttendanceDate()).checkInTime(savedAttendance.getClockIn()).checkOutTime(null).overtimeHours(BigDecimal.ZERO).build();
    }


    public AttendanceResponse clockOut(@Valid CheckOutRequest request) {
        Attendance attendance = attendanceRepository.findByWorkerIdAndClockOutIsNull(request.getWorkerId()).orElseThrow(() -> new BusinessValidationException("CLOCK_OUT_WITHOUT_CLOCK_IN", "Worker is not clocked in"));

        LocalDateTime clockOutTime = LocalDateTime.now();

        attendance.setClockOut(clockOutTime);

        long minutesWorked = Duration.between(attendance.getClockIn(), clockOutTime).toMinutes();

        BigDecimal totalHours = BigDecimal.valueOf(minutesWorked / 60.0).setScale(2, RoundingMode.HALF_UP);

        attendance.setTotalHoursWorked(totalHours);

        BigDecimal overtimeHours = BigDecimal.ZERO;

        if (totalHours.compareTo(BigDecimal.valueOf(8)) > 0) {

            overtimeHours = totalHours.subtract(BigDecimal.valueOf(8));
        }

        attendance.setOvertimeHours(overtimeHours);

        if (totalHours.compareTo(BigDecimal.valueOf(16)) > 0) {

            attendance.setFlagged(true);
        }

        if (overtimeHours.compareTo(BigDecimal.ZERO) > 0) {

            Worker worker = attendance.getWorker();

            LocalDate attendanceDate = attendance.getAttendanceDate();

            BigDecimal monthlyOvertimeAlreadyRecorded = overTimeRepository.getMonthlyOvertimeHours(worker.getId(), attendanceDate.getYear(), attendanceDate.getMonthValue());

            BigDecimal remainingCap = BigDecimal.valueOf(60).subtract(monthlyOvertimeAlreadyRecorded);

            if (remainingCap.compareTo(BigDecimal.ZERO) < 0) {
                remainingCap = BigDecimal.ZERO;
            }

            BigDecimal payableOvertimeHours = overtimeHours.min(remainingCap);

            if (payableOvertimeHours.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal hourlyRate = worker.getDailyWageRate().divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);

                BigDecimal firstTwoHours = payableOvertimeHours.min(BigDecimal.valueOf(2));

                BigDecimal remainingHours = payableOvertimeHours.subtract(firstTwoHours);

                if (remainingHours.compareTo(BigDecimal.ZERO) < 0) {
                    remainingHours = BigDecimal.ZERO;
                }

                BigDecimal firstTwoAmount = firstTwoHours.multiply(hourlyRate).multiply(BigDecimal.valueOf(1.5));

                BigDecimal remainingAmount = remainingHours.multiply(hourlyRate).multiply(BigDecimal.valueOf(2.0));

                BigDecimal totalAmount = firstTwoAmount.add(remainingAmount);

                OvertimeEntry overtimeEntry = new OvertimeEntry();

                overtimeEntry.setWorker(worker);

                overtimeEntry.setAttendance(attendance);

                overtimeEntry.setDate(attendanceDate);

                overtimeEntry.setOvertimeHours(payableOvertimeHours);

                overtimeEntry.setAmount(totalAmount);

                overtimeEntry.setOvertimeRateApplied(payableOvertimeHours.compareTo(BigDecimal.valueOf(2)) > 0 ? BigDecimal.valueOf(2.0) : BigDecimal.valueOf(1.5));

                overtimeEntry.setSettlementStatus(SettlementStatus.PENDING);

                overTimeRepository.save(overtimeEntry);
            }
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);

        cacheService.removeWorker(request.getWorkerId());

        return AttendanceResponse.builder().attendanceId(savedAttendance.getId()).workerId(savedAttendance.getWorker().getId()).workerName(savedAttendance.getWorker().getName()).siteId(savedAttendance.getSite().getId()).siteName(savedAttendance.getSite().getSiteName()).attendanceDate(savedAttendance.getAttendanceDate()).checkInTime(savedAttendance.getClockIn()).checkOutTime(savedAttendance.getClockOut()).overtimeHours(savedAttendance.getOvertimeHours()).build();
    }


    @Transactional(readOnly = true)
    public List<ActiveWorkerDto> getActiveWorkers() {

        return cacheService.getActiveWorkers();
    }

     // LF-203(main changes): Added pagination,
    //Pageable support, EntityGraph-based N+1 prevention, response metadata wrapper, default/max page size configuration,
    //and optimized attendance history queries.
    @Transactional(readOnly = true)
    public PaginatedResponse<AttendanceResponse> getAttendanceHistory(Long workerId, LocalDate from, LocalDate to,
                                                                      Pageable pageable) {

        Page<Attendance> attendancePage = attendanceRepository.findByWorkerIdAndAttendanceDateBetween(workerId, from, to, pageable);

        List<AttendanceResponse> content = attendancePage.getContent().stream().map(attendance -> AttendanceResponse.builder().attendanceId(attendance.getId()).workerId(attendance.getWorker().getId()).workerName(attendance.getWorker().getName()).siteId(attendance.getSite().getId()).siteName(attendance.getSite().getSiteName()).attendanceDate(attendance.getAttendanceDate()).checkInTime(attendance.getClockIn()).checkOutTime(attendance.getClockOut()).totalHoursWorked(attendance.getTotalHoursWorked()).overtimeHours(attendance.getOvertimeHours()).flagged(attendance.getFlagged()).build()).toList();

        return PaginatedResponse.<AttendanceResponse>builder().content(content).totalElements(attendancePage.getTotalElements()).totalPages(attendancePage.getTotalPages()).currentPage(attendancePage.getNumber()).pageSize(attendancePage.getSize()).build();
    }
}

