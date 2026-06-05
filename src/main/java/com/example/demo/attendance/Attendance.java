package com.example.demo.attendance;

import com.example.demo.site.Site;
import com.example.demo.worker.Worker;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "attendance_logs",
        uniqueConstraints = { // because we don't want multiple attendance record for each user in single day
                @UniqueConstraint(
                        name = "uk_worker_attendance_date",
                        columnNames = {
                                "worker_id",
                                "attendance_date"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_attendance_worker",
                        columnList = "worker_id"
                ),
                @Index(
                        name = "idx_attendance_site",
                        columnList = "site_id"
                ),
                @Index(
                        name = "idx_attendance_active",
                        columnList = "worker_id, clock_out"
                ),
                @Index(
                        name = "idx_attendance_active",
                        columnList = "worker_id, clock_out"
                ),
                @Index(
                        name = "idx_worker_date",
                        columnList = "worker_id, attendance_date"
                )
        }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "worker_id",
            nullable = false
    )
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "site_id",
            nullable = false
    )
    private Site site;

    @Column(nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    @Column(precision = 5, scale = 2)
    private BigDecimal totalHoursWorked;

    @Column(precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(nullable = false)
    private Boolean flagged = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.clockIn == null) {
            this.clockIn = now;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}