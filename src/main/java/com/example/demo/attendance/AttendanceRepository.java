package com.example.demo.attendance;

import com.example.demo.worker.Worker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.w3c.dom.stylesheets.LinkStyle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByWorkerIdAndClockOutIsNull(Long workerId);
    @EntityGraph(attributePaths = {
            "worker",
            "site"
    })
    Page<Attendance> findByWorkerIdAndAttendanceDateBetween(
            Long workerId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );
    List<Attendance> findByClockOutIsNull();

}
