package com.example.demo.cache;

import com.example.demo.attendance.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    // LF-202: fallback mechanism -  [ catch(exception) + attendanceRepository ] -> then -> fallback onto database!
    @Autowired
    private AttendanceRepository attendanceRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVE_WORKER_PREFIX = "active:worker:";
    private static final Duration TTL = Duration.ofHours(16);

    public void addWorker(ActiveWorkerDto worker) {

        try {

            String key = ACTIVE_WORKER_PREFIX + worker.getWorkerId();

            redisTemplate.opsForValue().set(key, worker, TTL);

            log.info("Worker {} added to active cache", worker.getWorkerId());

        } catch (Exception ex) {

            log.warn("Redis unavailable. Skipping cache write for worker {}", worker.getWorkerId(), ex);
        }
    }

    public void removeWorker(Long workerId) {

        try {

            String key = ACTIVE_WORKER_PREFIX + workerId;

            redisTemplate.delete(key);

            log.info("Worker {} removed from active cache", workerId);

        } catch (Exception ex) {

            log.warn("Redis unavailable. Skipping cache delete for worker {}", workerId, ex);
        }
    }

    public List<ActiveWorkerDto> getActiveWorkers() {

        try {

            Set<String> keys = redisTemplate.keys(ACTIVE_WORKER_PREFIX + "*");

            List<ActiveWorkerDto> activeWorkers = new ArrayList<>();

            if (keys == null || keys.isEmpty()) {
                return activeWorkers;
            }

            for (String key : keys) {

                Object value = redisTemplate.opsForValue().get(key);

                if (value instanceof ActiveWorkerDto worker) {
                    activeWorkers.add(worker);
                }
            }

            return activeWorkers;

        } catch (Exception ex) {

            log.warn("Redis unavailable. Falling back to database.", ex);

            return attendanceRepository.findByClockOutIsNull().stream().map(attendance -> ActiveWorkerDto.builder().workerId(attendance.getWorker().getId()).workerName(attendance.getWorker().getName()).siteId(attendance.getSite().getId()).siteName(attendance.getSite().getSiteName()).clockInTime(attendance.getClockIn()).build()).toList();
        }
    }

    public void invalidateWorker(Long workerId) {

        removeWorker(workerId);
    }
}