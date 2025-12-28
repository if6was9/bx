package bx.util;

import java.lang.reflect.Modifier;
import java.util.Optional;

public class Classes {

  private Classes() {}

  public static boolean isAbstract(Class<?> clazz) {
    return Modifier.isAbstract(clazz.getModifiers());
  }

  public static Optional<String> findEnclosingClassName() {
    Throwable t = new Throwable();
    boolean found = false;
    StackTraceElement[] elements = t.getStackTrace();

    for (int i = 0; i < elements.length; i++) {

      if (found) {
        if (!elements[i].getMethodName().equals("findEnclosingClassName")) {
          return Optional.ofNullable(elements[i].getClassName());
        }
      }
      if (elements[i].getMethodName().equals("findEnclosingClassName")) {
        found = true;
      }
    }

    return Optional.empty();
  }
}
