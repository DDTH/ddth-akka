package com.github.ddth.akka.scheduling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.ddth.akka.scheduling.CronFormat;
import com.github.ddth.akka.scheduling.WorkerCoordinationPolicy;

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

    /**
     * If {@code true}, the first "tick" will fire as soon as the actor starts,
     * ignoring tick-matching check.
     * 
     * @return
     * @since 0.1.1
     */
    boolean runFirstTimeRegardlessScheduling() default false;

    /**
     * 
     * @return
     * @since 0.1.1
     */
    WorkerCoordinationPolicy getWorkerCoordinationPolicy() default WorkerCoordinationPolicy.TAKE_ALL_TASKS;
}
