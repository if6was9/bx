package bx.util;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.slf4j.Logger;

public class ParallelConsumer<T> {

  static Logger logger = Slogger.forEnclosingClass();

  List<T> queue;
  int threads = Runtime.getRuntime().availableProcessors();

  Consumer<T> consumer;

  AtomicBoolean complete = new AtomicBoolean(false);
  AtomicBoolean cancelled = new AtomicBoolean(false);
  AtomicBoolean started = new AtomicBoolean(false);

  AtomicInteger threadNameCount = new AtomicInteger();
  String threadName = "ParallelConsumer";

  public static <T> ParallelConsumer<T> create(Collection<T> src) {

    ParallelConsumer<T> p = new ParallelConsumer<T>();
    p.queue = Lists.newLinkedList(src);
    return p;
  }

  public synchronized int getRemainingCount() {

    return queue.size();
  }

  private String nextThreadName() {
    return String.format("%s-%s", this.threadName, threadNameCount.getAndIncrement());
  }

  private void onThreadStart() {
    Thread.currentThread().setName(nextThreadName());

    started.set(true);
    while (complete.get() == false && cancelled.get() == false) {

      T it = null;
      synchronized (this) {
        if (queue.isEmpty()) {
          logger.atDebug().log("queue processing is complete");
          complete.set(true);
          notifyAll();
          return;
        }
        it = queue.removeFirst();
      }

      try {
        consumer.accept(it);
      } catch (Exception e) {
        logger.atWarn().setCause(e).log();
      }
    }
  }

  public ParallelConsumer<T> threadName(String name) {
    this.threadName = name;
    return this;
  }

  public ParallelConsumer<T> threadCount(int threadCount) {
    this.threads = threadCount;
    return this;
  }

  public Future<ParallelConsumer<T>> consume(Consumer<T> consumer) {
    this.consumer = consumer;
    for (int i = 0; i < threads; i++) {
      Thread.ofVirtual().start(this::onThreadStart);
    }
    Future<ParallelConsumer<T>> f =
        new Future<ParallelConsumer<T>>() {

          long createTs = System.currentTimeMillis();

          @Override
          public boolean cancel(boolean mayInterruptIfRunning) {
            synchronized (ParallelConsumer.this) {
              if (complete.get()) {
                return false;
              }
              if (cancelled.get()) {
                return true;
              }
              if (started.get()) {

                cancelled.set(true);
                if (mayInterruptIfRunning) {
                  notifyAll();
                }

              } else {
                cancelled.set(true);
              }
              return true;
            }
          }

          @Override
          public boolean isCancelled() {
            return cancelled.get();
          }

          @Override
          public boolean isDone() {
            return complete.get();
          }

          @Override
          public ParallelConsumer<T> get() throws InterruptedException, ExecutionException {
            try {
              return get(6, TimeUnit.HOURS);
            } catch (TimeoutException e) {
              throw new InterruptedException("timeout");
            }
          }

          @Override
          public ParallelConsumer<T> get(long timeout, TimeUnit unit)
              throws InterruptedException, ExecutionException, TimeoutException {
            while (TimeUnit.MILLISECONDS.convert(timeout, unit) + createTs
                > System.currentTimeMillis()) {
              synchronized (ParallelConsumer.this) {
                if (ParallelConsumer.this.complete.get()) {
                  return ParallelConsumer.this;
                }
              }
            }
            throw new InterruptedException();
          }
        };
    return f;
  }
}
