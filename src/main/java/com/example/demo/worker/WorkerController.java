package com.example.demo.worker;


import com.example.demo.worker.dto.UpdateWorkerRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {
    @Autowired
    private WorkerService workerService;

    @PutMapping("/{workerId}") // just to present the invalidation on update(business requirement)
    public ResponseEntity<Worker> updateWorker(@PathVariable Long workerId, @Valid @RequestBody UpdateWorkerRequest request

    ) {
        return  new ResponseEntity<>(workerService.updateWorker(
                workerId,
                request ), HttpStatus.OK);
}
}
