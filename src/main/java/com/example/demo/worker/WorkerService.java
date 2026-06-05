package com.example.demo.worker;

import com.example.demo.cache.ActiveWorkerCacheService;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.worker.dto.UpdateWorkerRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkerService {

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private ActiveWorkerCacheService cacheService;

    @Transactional
    public Worker updateWorker(Long workerId, @Valid UpdateWorkerRequest request) {

        Worker worker = workerRepository.findById(workerId).orElseThrow(() -> new ResourceNotFoundException("Worker not found " + "with id: " + workerId));

        worker.setName(request.getName());

        worker.setDesignation(request.getDesignation());

        worker.setDailyWageRate(request.getDailyWageRate());

        Worker savedWorker = workerRepository.save(worker);

        // CACHE INVALIDATION
        cacheService.removeWorker(workerId);

        return savedWorker;
    }
}
