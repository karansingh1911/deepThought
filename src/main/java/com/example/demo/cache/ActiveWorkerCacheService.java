package com.example.demo.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveWorkerCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVE_WORKER_PREFIX = "active:worker:";
    private static final Duration TTL = Duration.ofHours(16);

    public void addWorker(ActiveWorkerDto worker) {

        String key = ACTIVE_WORKER_PREFIX + worker.getWorkerId();

        redisTemplate.opsForValue().set(
                key,
                worker,
                TTL
        );

        log.info(
                "Worker {} added to active cache",
                worker.getWorkerId()
        );
    }

    public void removeWorker(Long workerId) {

        String key = ACTIVE_WORKER_PREFIX + workerId;

        redisTemplate.delete(key);

        log.info(
                "Worker {} removed from active cache",
                workerId
        );
    }

    public List<ActiveWorkerDto> getActiveWorkers() {

        Set<String> keys =
                redisTemplate.keys(
                        ACTIVE_WORKER_PREFIX + "*"
                );

        List<ActiveWorkerDto> activeWorkers =
                new ArrayList<>();

        if (keys == null || keys.isEmpty()) {
            return activeWorkers;
        }

        for (String key : keys) {

            Object value =
                    redisTemplate.opsForValue().get(key);

            if (value instanceof ActiveWorkerDto worker) {
                activeWorkers.add(worker);
            }
        }

        return activeWorkers;
    }

    public void invalidateWorker(Long workerId) {

        removeWorker(workerId);
    }
}