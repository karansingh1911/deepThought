package com.example.demo.attendance;

import com.example.demo.cache.ActiveWorkerCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceExpiryScheduler {

    private final AttendanceRepository attendanceRepository;
    private final ActiveWorkerCacheService cacheService;

    @Scheduled(fixedRate = 300000) // every 5 min
    public void flagExpiredAttendances() {

        List<Attendance> activeAttendances = attendanceRepository.findByClockOutIsNull();

        for (Attendance attendance : activeAttendances) {

            long hours = Duration.between(attendance.getClockIn(), LocalDateTime.now()).toHours();

            if (hours >= 16) {

                attendance.setFlagged(true);

                attendanceRepository.save(attendance);

                cacheService.removeWorker(attendance.getWorker().getId());
            }
        }
    }
}