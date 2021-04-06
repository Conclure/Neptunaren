package me.conclure.neptunaren;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchedulerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerAdapter.class);
  private final ExecutorService syncExecutor;
  private final ScheduledThreadPoolExecutor scheduler;
  private final ExecutorService schedulerWorkerPool;
  private final ExecutorService asyncExecutor;

  public SchedulerAdapter() {
    this.syncExecutor = Executors.newSingleThreadExecutor();
    this.asyncExecutor = new ForkJoinPool(
        32,
        ForkJoinPool.defaultForkJoinWorkerThreadFactory,
        (t, e) -> LOGGER.error(e.getMessage(), e),
        false);

    this.scheduler = new ScheduledThreadPoolExecutor(
        1,
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("daemon-scheduler").build());
    this.schedulerWorkerPool = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("daemon-scheduler-worker-%d")
            .build());
  }

  public ExecutorService async() {
    return this.asyncExecutor;
  }

  public ExecutorService sync() {
    return this.syncExecutor;
  }

  public Future<?> runAsync(Runnable runnable) {
    return this.asyncExecutor.submit(runnable);
  }

  public ScheduledFuture<?> runAsyncLater(Runnable runnable, long delay, TimeUnit unit) {
    return this.scheduler.schedule(() -> this.schedulerWorkerPool.submit(runnable), delay, unit);
  }

  public ScheduledFuture<?> runAsyncRepeat(Runnable runnable, long interval, TimeUnit unit) {
    return this.scheduler
        .scheduleWithFixedDelay(() -> this.schedulerWorkerPool.submit(runnable), interval, interval,
            unit);
  }

  public Future<?> run(Runnable runnable) {
    return this.syncExecutor.submit(runnable);
  }

  public ScheduledFuture<?> runLater(Runnable runnable, long delay, TimeUnit unit) {
    return this.scheduler.schedule(() -> this.syncExecutor.submit(runnable), delay, unit);
  }

  public ScheduledFuture<?> runRepeat(Runnable runnable, long interval, TimeUnit unit) {
    return this.scheduler
        .scheduleWithFixedDelay(() -> this.syncExecutor.submit(runnable), interval, interval, unit);
  }

  private static boolean shutdownExecutorService(ExecutorService executorService,
      long awaitDuration, TimeUnit unit) {
    executorService.shutdown();

    try {
      return executorService.awaitTermination(awaitDuration, unit);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return false;
  }

  public boolean shutdownScheduler(long awaitDuration, TimeUnit unit) {
    return shutdownExecutorService(this.scheduler,awaitDuration,unit);
  }

  public boolean shutdownSchedulerWorker(long awaitDuration, TimeUnit unit) {
    return shutdownExecutorService(this.schedulerWorkerPool,awaitDuration,unit);
  }

  public boolean shutdownAsyncExecutor(long awaitDuration, TimeUnit unit) {
    return shutdownExecutorService(this.asyncExecutor,awaitDuration,unit);
  }

  public boolean shutdownSyncExecutor(long awaitDuration, TimeUnit unit) {
    return shutdownExecutorService(this.syncExecutor,awaitDuration,unit);
  }

  public void terminateScheduler() {
    this.scheduler.shutdownNow();
  }

  public void terminateSchedulerWorker() {
    this.schedulerWorkerPool.shutdownNow();
  }

  public void terminateAsyncExecutor() {
    this.asyncExecutor.shutdownNow();
  }

  public void terminateSyncExecutor() {
    this.syncExecutor.shutdownNow();
  }
}