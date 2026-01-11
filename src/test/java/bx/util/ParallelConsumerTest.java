package bx.util;

import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParallelConsumerTest {

  @Test
  public void testIt() throws Exception {

    var f =
        ParallelConsumer.create(IntStream.range(0, 10000).boxed().toList())
        .threadName("test")
        .threadCount(3)
            .consume(
                x -> {
                  System.out.println(Thread.currentThread() + " " + x);
                });

 
    ParallelConsumer<Integer> x = f.get();

   Assertions.assertThat(f.get()).isSameAs(x);
    Assertions.assertThat(x.getRemainingCount()).isEqualTo(0);
   Assertions.assertThat(f.isDone()).isTrue();
   Assertions.assertThat(f.isCancelled()).isFalse();
   
  }
}
