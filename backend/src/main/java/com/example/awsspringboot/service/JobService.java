package com.example.awsspringboot.service;

import com.example.awsspringboot.model.JobItem;
import com.example.awsspringboot.model.JobStatus;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class JobService {
  private final ConcurrentMap<String, JobItem> jobs = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public JobItem createJob(String message) {
    String now = Instant.now().toString();
    String jobId = UUID.randomUUID().toString();

    JobItem item = new JobItem();
    item.setJobId(jobId);
    item.setMessage(message);
    item.setStatus(JobStatus.PENDING);
    item.setCreatedAt(now);
    item.setUpdatedAt(now);

    jobs.put(jobId, item);

    scheduler.schedule(() -> markProcessing(jobId), 300, TimeUnit.MILLISECONDS);
    scheduler.schedule(() -> markCompleted(jobId), 1800, TimeUnit.MILLISECONDS);

    return item;
  }

  public Optional<JobItem> getJob(String jobId) {
    return Optional.ofNullable(jobs.get(jobId));
  }

  private void markProcessing(String jobId) {
    jobs.computeIfPresent(jobId, (_id, item) -> {
      String now = Instant.now().toString();
      item.setStatus(JobStatus.PROCESSING);
      item.setProcessingAt(now);
      item.setUpdatedAt(now);
      return item;
    });
  }

  private void markCompleted(String jobId) {
    jobs.computeIfPresent(jobId, (_id, item) -> {
      String now = Instant.now().toString();
      item.setStatus(JobStatus.COMPLETED);
      item.setProcessedAt(now);
      item.setUpdatedAt(now);
      item.setResult("Processed message for job " + jobId);
      return item;
    });
  }

  @PreDestroy
  public void shutdown() {
    scheduler.shutdownNow();
  }
}
