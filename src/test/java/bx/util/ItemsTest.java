package bx.util;

import com.google.common.collect.Lists;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ItemsTest {

  @Test
  public void testList() {
    Assertions.assertThat(Items.stream(List.of(), 0)).hasSize(0);
    Assertions.assertThat(Items.stream(null, 0)).hasSize(0);
    Assertions.assertThat(Items.stream(null)).hasSize(0);
    Assertions.assertThat(Items.list(null)).hasSize(0);
    Assertions.assertThat(Items.list(null, 5)).hasSize(0);
    Assertions.assertThat(Items.list(List.of(), 5)).hasSize(0);

    Assertions.assertThat(Items.list(List.of("a", "b", "c"), -3).getFirst().index()).isEqualTo(-3);
  }

  @Test
  public void testIt() {

    System.out.println(Item.of("hello", 1));

    Assertions.assertThat(Item.of(null, 0).get()).isNull();

    Items.stream(List.of("a", "b", "c"), 5)
        .forEach(
            it -> {
              if (it.get().equals("a")) {
                Assertions.assertThat(it.index()).isEqualTo(5);
              }
              if (it.get().equals("b")) {
                Assertions.assertThat(it.index()).isEqualTo(6);
              }
              if (it.get().equals("c")) {
                Assertions.assertThat(it.index()).isEqualTo(7);
              }
            });
  }

  @Test
  public void testLong() {

    int a = (int) Long.MAX_VALUE;

    Assertions.assertThat(Item.of("big", Long.MAX_VALUE).indexLong()).isEqualTo(Long.MAX_VALUE);

    try {
      Item.of("big", Long.MAX_VALUE).index();
      Assertions.failBecauseExceptionWasNotThrown(IndexOutOfBoundsException.class);
    } catch (IndexOutOfBoundsException ignore) {
    }
    try {
      Item.of("big", Long.MIN_VALUE).index();
      Assertions.failBecauseExceptionWasNotThrown(IndexOutOfBoundsException.class);
    } catch (IndexOutOfBoundsException ignore) {
    }
  }

  @Test
  public void testIx() {
    List<Integer> list = Lists.newArrayList();
    for (int i = 0; i < 10; i++) {
      list.add(i);
    }

    List<Item<Integer>> itemList = Items.list(list);

    Assertions.assertThat(itemList.size()).isEqualTo(list.size());
    for (int i = 0; i < list.size(); i++) {
      Assertions.assertThat(list.get(i)).isSameAs(itemList.get(i).get());
      Assertions.assertThat(itemList.get(i).index()).isEqualTo(i);
    }
  }
}
