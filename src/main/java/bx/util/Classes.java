package bx.util;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Classes {

  private Classes() {}

  public static boolean isAbstract(Class<?> clazz) {
    return Modifier.isAbstract(clazz.getModifiers());
  }

  public static Optional<String> findEnclosingClassName() {
    return findEnclosingClassNameExcluding(Set.of());
  }

  public static Optional<String> findEnclosingClassNameExcluding(Set<Class<?>> excludes) {
    if (excludes == null) {
      excludes = Set.of();
    }
    Throwable t = new Throwable();
    boolean found = false;
    StackTraceElement[] elements = t.getStackTrace();

    Set<String> classNames = excludes.stream().map(z -> z.getName()).collect(Collectors.toSet());
    for (int i = 0; i < elements.length; i++) {

      String steClassName = elements[i].getClassName();
      String steMethodName = elements[i].getMethodName();
      if (found) {
        if (elements[i].getMethodName().equals("findEnclosingClassName")) {
          // do nothing
        } else if (classNames.contains(steClassName)) {
          // do nothing
        } else if (Classes.class.getName().equals(steClassName)) {
          // do nothing
        } else if (Slogger.class.getName().equals(steClassName)) {
          // do nothing
        } else {
          return Optional.ofNullable(steClassName);
        }
      }
      if (Classes.class.getName().equals(steClassName)
          && steMethodName.startsWith("findEnclosingClassName")) {
        found = true;
      }
    }

    return Optional.empty();
  }
}
