package bx.util;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class Slogger {

	
	static Logger logger = LoggerFactory.getLogger(Slogger.class);
	
	static Set<String> warnings = Sets.newConcurrentHashSet();
	
	private Slogger() {
		
	}
	public static Logger forEnclosingClass() {
		
		Throwable t = new Throwable();
		boolean found=false;
		for (StackTraceElement x: t.getStackTrace()) {
			
			if (found) {
			
				if (x.getMethodName().equals("<init>")) {
					if (!warnings.contains(x.getClassName())) {
						logger.atWarn().log("logger instance should be static in {}",x.getClassName());
						synchronized (warnings) {
							warnings.add(x.getClassName());
						}
					}
					
				}
				System.out.println(x);
				return LoggerFactory.getLogger(x.getClassName());
			}
			if (x.getClassName().equals(Slogger.class.getName()) && x.getMethodName().equals("forEnclosingClass")) {
				found=true;
			}
		
		}
		
		return LoggerFactory.getLogger(Slogger.class);
		
		
	}
}
