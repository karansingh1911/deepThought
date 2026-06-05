package com.example.demo.overtimeEntry;

import com.example.demo.attendance.Attendance;
import com.example.demo.common.enums.SettlementStatus;
import com.example.demo.worker.Worker;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(
        name = "overtime_entries",
        indexes = {
                @Index(
                        name = "idx_overtime_worker_date",
                        columnList = "worker_id, date"
                ),
                @Index(
                        name = "idx_overtime_status",
                        columnList = "settlement_status"
                )
        }
)
public class OvertimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeHours;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeRateApplied;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus= SettlementStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;

    //lifecycle methods
    @PrePersist
    public void onCreate(){
        this.createdAt= LocalDateTime.now();
        this.updatedAt= LocalDateTime.now();

    }
    // this alone can solve the issue -  of once settled, cannot be modified()
    // better than service layer validation!!!
    @PreUpdate
    public void validateUpdate() {

        if (
                this.settlementStatus
                        == SettlementStatus.SETTLED
        ) {

            throw new IllegalStateException(
                    "Settled overtime entries cannot be modified"
            );
        }

        this.updatedAt =
                LocalDateTime.now();
    }
}