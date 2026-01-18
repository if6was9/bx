package bx.util;

import java.util.Set;

public class Mask {

  static final String FIVE_STAR = "*****";

  static final Set<String> sensitiveStrings =
      Set.of(
          "key",
          "passwd",
          "token",
          "password",
          "passphrase",
          "secret",
          "bearer",
          "clientid",
          "appid",
          "jwt",
          "do_pat");

  public static final String mask(String input) {

    if (S.isBlank(input)) {
      return FIVE_STAR;
    }

    if (input.length() > 14) {
      return input.substring(0, 2) + FIVE_STAR + input.substring(input.length() - 2);
    }

    return FIVE_STAR;
  }

  public static String mask(String key, String val) {
    if (isSensitiveKey(key)) {
      return mask(val);
    }
    return val;
  }

  public static boolean isSensitiveKey(String k) {
    if (k == null) {
      return false;
    }

    return sensitiveStrings.stream()
        .map(s -> s.toLowerCase().trim().replace("-", "").replace("_", ""))
        .anyMatch(
            p ->
                S.notBlank(k)
                    .orElse("")
                    .toLowerCase()
                    .trim()
                    .replace("-", "")
                    .replace("_", "")
                    .contains(p));
  }
}
