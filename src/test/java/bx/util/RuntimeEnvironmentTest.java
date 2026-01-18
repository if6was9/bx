package bx.util;

import java.io.File;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class RuntimeEnvironmentTest extends BxTest {

  @Test
  public void testIt() {
    RuntimeEnvironment re = RuntimeEnvironment.get();
    Assertions.assertThat(re.isRunningInKubernetes()).isFalse();
    Assertions.assertThat(re.isRunningInContainer()).isFalse();
    Assertions.assertThat(re.isSourceEnvironment()).isTrue();
    Assertions.assertThat(re.isRunningInLambda()).isFalse();
    Assertions.assertThat(re.isRunningInFly()).isFalse();
    if (System.getenv("CI") != null) {
      Assertions.assertThat(re.isMac()).isFalse();
      Assertions.assertThat(re.isLinux()).isTrue();
      Assertions.assertThat(re.isDesktopSupported()).isFalse();
    }

    if (new File("/Applications").exists() && new File("/Library").exists()) {
      Assertions.assertThat(re.isMac()).isTrue();
      Assertions.assertThat(re.isLinux()).isFalse();
      Assertions.assertThat(re.isDesktopSupported()).isTrue();
    }
  }
}
