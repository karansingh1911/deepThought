package com.example.demo.attendance;

import com.example.demo.attendance.dto.AttendanceResponse;
import com.example.demo.attendance.dto.CheckInRequest;
import com.example.demo.attendance.dto.CheckOutRequest;
import com.example.demo.cache.ActiveWorkerDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(@Valid @RequestBody CheckInRequest request) {
        return new ResponseEntity<>(attendanceService.clockIn(request), HttpStatus.CREATED);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(@Valid @RequestBody CheckOutRequest request) {
        return new ResponseEntity<>(attendanceService.clockOut(request), HttpStatus.CREATED);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ActiveWorkerDto>> getActiveWorkers() {
        return new ResponseEntity<>(attendanceService.getActiveWorkers(), HttpStatus.OK);
    }

    @GetMapping("/log")
    public ResponseEntity<Page<AttendanceResponse>> getAttendanceHistory(

            @RequestParam Long workerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size

    ) {

        Pageable pageable = PageRequest.of(page, size);

        return new ResponseEntity<>(attendanceService.getAttendanceHistory(workerId, from, to, pageable),HttpStatus.OK);
    }

}
