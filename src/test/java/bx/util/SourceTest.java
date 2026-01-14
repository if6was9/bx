package bx.util;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.GeneralCodingRules;

@AnalyzeClasses(packages = "bx")
public class SourceTest {

  public String[] BANNED_PACKAGES =
      new String[] {
        "org.apache.commons.lang3",
        "org.apache.commons.compress.utils",
        "org.assertj.core.internal",
        "org.assertj.core.util",
        "org.testcontainers.shaded.com.google.common.collect",
        "com.tngtech.archunit.thirdparty.com.google.common.collect"
      };

  @ArchTest
  private final ArchRule noJavaUtilLogging =
      GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

  @ArchTest
  private final ArchRule noStdoutStderr =
      ArchRuleDefinition.noClasses()
          .that()
          .haveSimpleNameNotEndingWith("Test")
          .and()
          .doNotHaveSimpleName("ConsoleQuery")
          .should(GeneralCodingRules.ACCESS_STANDARD_STREAMS);

  @ArchTest
  private final ArchRule bannedPackages =
      ArchRuleDefinition.noClasses()
          .that()
          .doNotHaveSimpleName("SourceTest")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(BANNED_PACKAGES);

  @ArchTest
  private final ArchRule duckDbDeps =
      ArchRuleDefinition.noClasses()
          .that()
          .resideOutsideOfPackage("bx.sql.duckdb")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.duckdb");
}
