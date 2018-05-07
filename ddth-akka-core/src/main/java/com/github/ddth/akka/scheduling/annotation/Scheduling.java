package com.github.ddth.akka.scheduling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ddth.akka.scheduling.CronFormat;

/**
 * Annotation to define worker's scheduling in {@code CronFormat}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scheduling {
    /**
     * Scheduling in {@link CronFormat}.
     * 
     * @return
     */
    String value();
}
