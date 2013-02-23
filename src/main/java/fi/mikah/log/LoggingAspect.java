package fi.mikah.log;

/*

  Copyright (c) 2013 Mika Hämäläinen

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Aspect that implements logging for {@code CallLogging} annotated methods.
 *
 * @author Mika Hämäläinen <mika.hamalainen@gmail.com>
 *
 */
@Aspect
@Component
public class LoggingAspect {
  /** List of loggers. */
  private static final Map<Class<?>, Logger> loggers = new HashMap<Class<?>, Logger>();

  /**
   * Entry logging.
   *
   * @param joinPoint The join point for the method.
   * @param callLogging The call logging annotation.
   */
  @Before(value = "@annotation(callLogging)", argNames = "joinPoint, callLogging")
  public void logEntry(final JoinPoint joinPoint, final CallLogging callLogging) {
    Logger log = getLogger(joinPoint);
    Level level = Level.toLevel(callLogging.value().toString());

    if (log.isEnabledFor(level)) {
      log.log(level, "Entering method "
          + joinPoint.getSignature().getName()
          + "["
          + getArgs(joinPoint.getArgs())
          + "]");
    }
  }

  /**
   * Return logging.
   *
   * @param joinPoint The join point for the method.
   * @param callLogging The call logging annotation.
   * @param returnValue The return value of the called method.
   */
  @AfterReturning(value = "@annotation(callLogging)",
      argNames = "joinPoint, callLogging, returnValue", returning = "returnValue")
  public void logExitAfterReturn(final JoinPoint joinPoint,
      CallLogging callLogging, Object returnValue) {
    Logger log = getLogger(joinPoint);
    Level level = Level.toLevel(callLogging.value().toString());

    if (log.isEnabledFor(level)) {
      MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      Class<?> returnType = signature.getReturnType();
      if (returnType.getName().equals("void")) {
        log.log(level, "Exiting method "
            + joinPoint.getSignature().getName()
            + "[void]");
      } else {
        log.log(level, "Exiting method "
            + joinPoint.getSignature().getName()
            + "[" + returnValue + "]");
      }
    }
  }

  /**
   * Throw logging.
   *
   * @param joinPoint The join point for the method.
   * @param callLogging The call logging annotation.
   * @param throwable The return value of the called method.
   */
  @AfterThrowing(value = "@annotation(callLogging)",
      argNames = "joinPoint, callLogging, throwable", throwing = "throwable")
  public void logExitAfterThrowing(final JoinPoint joinPoint,
      CallLogging callLogging, Throwable throwable) {
    Logger log = getLogger(joinPoint);
    Level level = Level.toLevel(callLogging.value().toString());

    if (log.isEnabledFor(level)) {
      log.log(level, "Exiting method "
          + joinPoint.getSignature().getName()
          + "[" + throwable.getCause() + "]");
    }
  }

  /**
   * Creates a string presentation of method arguments.
   *
   * @param args The array of arguments.
   * @return A new string constructed from the arguments.
   */
  private String getArgs(Object... args) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.length; ++i) {
      sb.append(args[i]);
      if (i + 1 < args.length) {
        sb.append(", ");
      }
    }

    return sb.toString();
  }

  /**
   * Returns a logger for the join point. Gets a logger for the class of the
   * called method.
   *
   * @param joinPoint The join point for the method.
   * @return The logger.
   */
  private Logger getLogger(final JoinPoint joinPoint) {
    Class<?> clazz = joinPoint.getTarget().getClass();
    Logger log = loggers.get(clazz);
    if (log == null) {
      log = Logger.getLogger(clazz);
      loggers.put(clazz, log);
    }

    return log;
  }
}
