package bx.util;

import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.slf4j.Logger;

public class RuntimeEnvironment {

  static Logger logger = Slogger.forEnclosingClass();
  public static final RuntimeEnvironment instance = new RuntimeEnvironment();
  Config config;

  Supplier<Boolean> kubeSupplier =
      Suppliers.memoize(
          () -> {
            if (!isRunningInContainer()) {
              return false;
            }
            if (config.get("KUBERNETES_PORT").isPresent()
                && config.get("KUBERNETES_SERVICE_HOST").isPresent()) {
              return true;
            }
            return false;
          });
  Supplier<Boolean> containerSupplier =
      Suppliers.memoize(
          () -> {
            try {

              String s =
                  S.notNull(
                          Files.asCharSource(new File("/proc/1/cgroup"), StandardCharsets.UTF_8)
                              .read())
                      .orElse("")
                      .strip();

              logger.atDebug().log("/proc/1/cgroup: <<%s>>", s);
              if (s.equalsIgnoreCase("0::/")) {
                logger.atDebug().log("inside container!");
                return true;
              }
            } catch (FileNotFoundException e) {
              // ignore
            } catch (IOException | RuntimeException e) {
              // ignore
              logger.atDebug().setCause(e).log("exception (can be ignored)");
            }

            try {
              if (new File("/.dockerenv").exists()) {
                logger.atDebug().log("found /.dockerenv");
                return true;
              }
            } catch (Exception e) {
              logger.atDebug().setCause(e).log("exception (can be ignored)");
            }

            return false;
          });

  Supplier<Boolean> unitTestEnvironment =
      Suppliers.memoize(
          () -> {
            try {

              Class.forName("org.junit.jupiter.api.Test");
              return true;
            } catch (ClassNotFoundException e) {
              // ignore
            } catch (Exception e) {
              logger.atDebug().setCause(e).log("exception (can be ignored)");
            }
            return false;
          });

  public RuntimeEnvironment() {
    this(Config.get());
  }

  RuntimeEnvironment(Config cfg) {
    this.config = cfg;
  }

  public static RuntimeEnvironment get() {
    return instance;
  }

  public boolean isSourceEnvironment() {
    File f = new File("./src/main/java/bx");
    return f.exists();
  }

  public boolean isRunningInFly() {
    return config.get("FLY_APP_NAME").isPresent();
  }

  public boolean isRunningInKubernetes() {
    return kubeSupplier.get();
  }

  public boolean isRunningInContainer() {
    return containerSupplier.get();
  }

  public boolean isUnitTestEnvironment() {
    return unitTestEnvironment.get();
  }

  private String getOsProperty() {
    return config.get("os.name").orElse("").toLowerCase().trim();
  }

  public boolean isLinux() {
    return getOsProperty().contains("linux");
  }

  public boolean isMac() {
    return getOsProperty().contains("mac") || getOsProperty().contains("os x");
  }

  public boolean isCIEnvironment() {
    if (config.get("GITHUB_WORKFLOW").isPresent()) {
      return true;
    } else if (config.get("CI").isPresent()) {
      return true;
    }

    return false;
  }

  public boolean isRunningInLambda() {
    return lambdaSupplier.get();
  }

  Supplier<Boolean> lambdaSupplier =
      Suppliers.memoize(
          () -> {
            return config.get("LAMBDA_TASK_ROOT").isPresent();
          });

  Supplier<Boolean> desktopSupported =
      Suppliers.memoize(
          () -> {
            try {
              return Desktop.isDesktopSupported();
            } catch (Exception e) {
              logger.atInfo().setCause(e).log();
            }
            return false;
          });

  public boolean isDesktopSupported() {
    return desktopSupported.get();
  }
}
