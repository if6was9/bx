package bx.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slogger {

  static Logger logger = LoggerFactory.getLogger(Slogger.class);

  static Set<String> warnings = Sets.newConcurrentHashSet();

  private Slogger() {}

  public static Logger forEnclosingClass() {

	 
	  
    Logger logger = LoggerFactory.getLogger(
        Classes.findEnclosingClassNameExcluding(Set.of(Slogger.class)).orElse(Slogger.class.getName()));
    
    Preconditions.checkState(!Slogger.class.getName().equals(logger.getName()));
    Preconditions.checkState(!Classes.class.getName().equals(logger.getName()));
    
    return logger;
  }
}
