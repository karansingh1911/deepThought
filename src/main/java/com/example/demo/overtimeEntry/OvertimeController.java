package com.example.demo.overtimeEntry;

import com.example.demo.overtimeEntry.dto.OvertimeSummaryResponse;
import com.example.demo.overtimeEntry.dto.SettlementResponse;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RequestMapping("/api/overtime")
@RestController
public class OvertimeController {
    @Autowired
    OverTimeService overTimeService;


    @GetMapping("/summary/{workerId}")
    public ResponseEntity<OvertimeSummaryResponse> getSummary(@PathVariable Long workerId, @RequestParam String month){
        return new ResponseEntity<>(overTimeService.getMonthlySummary(workerId, YearMonth.parse(month)), HttpStatus.OK);
    }

    @PostMapping("/settle/{workerId}")
    public ResponseEntity<SettlementResponse> settleOvertime(@PathVariable Long workerId, @RequestParam String month){
        return new ResponseEntity<>(overTimeService.settleMonthlyOvertime(workerId,YearMonth.parse(month)),HttpStatus.CREATED);
    }

}
