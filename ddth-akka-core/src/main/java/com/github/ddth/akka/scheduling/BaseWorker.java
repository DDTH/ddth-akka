package com.github.ddth.akka.scheduling;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.BaseActor;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.inmem.InmemDLock;

import scala.concurrent.duration.Duration;

/**
 * Base class to implement workers.
 *
 * <p>
 * Worker implementation:
 * <ul>
 * <li>Worker is scheduled to perform task. Scheduling configuration is in
 * Cron-like format (see {@link CronFormat} and {@link #getScheduling()}).</li>
 * <li>At every "tick", worker receives a "tick" message (see
 * {@link TickMessage}). The "tick" message carries a timestamp and a unique id.
 * This timestamp is checked against worker's scheduling configuration so
 * determine that worker's task should be fired off.</li>
 * <li>If worker's task is due, {@link #doJob(TickMessage)} is called. Sub-class
 * implements this method to perform its own business logic.
 * <ul>
 * <li>Before calling {@link #doJob(TickMessage)}, a lock will be acquired (see
 * {@link #lock(String, long)} and {@link #isRunOnlyWhenNotBusy()}) so that at
 * one given time only one execution of {@link #doJob(TickMessage)} is allowed
 * (same affect as {@code synchronized
 * doJob(TickMessage)}).</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class BaseWorker extends BaseActor {

    private Logger LOGGER = LoggerFactory.getLogger(BaseWorker.class);
    public final static long DEFAULT_DLOCK_TIME_MS = 10000;

    private IDLock dlock;
    private long dlockTimeMs = DEFAULT_DLOCK_TIME_MS;

    private final Collection<Class<?>> channelSubscriptions = Collections
            .singleton(TickMessage.class);

    public BaseWorker() {
    }

    public BaseWorker(IDLock dlock, long dlockTimeMs) {
        this.dlock = dlock;
        this.dlockTimeMs = dlockTimeMs;
    }

    /**
     * Special "tick" message to be sent only once when actor starts.
     */
    protected static class FirstTimeTickMessage extends TickMessage {
        private static final long serialVersionUID = "0.1.0".hashCode();
    }

    /**
     * If {@code true}, the first "tick" will fire as soon as the actor starts,
     * ignoring tick-matching check.
     * 
     * <p>
     * If worker is annotated by {@link Scheduling}, this method returns value
     * of {@link Scheduling#runFirstTimeRegardlessScheduling()}. Otherwise this
     * method returns {@code false}. Sub-class may override this method to
     * customize its own business logic.
     * </p>
     *
     * @return
     */
    protected boolean isRunFirstTimeRegardlessScheduling() {
        Scheduling[] schedulings = getClass().getAnnotationsByType(Scheduling.class);
        if (schedulings != null && schedulings.length > 0) {
            return schedulings[0].runFirstTimeRegardlessScheduling();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Class<?>> channelSubscriptions() {
        return channelSubscriptions;
    }

    /**
     * Create & Initialize a distributed-lock instance associated with this
     * worker.
     * 
     * @return
     */
    protected IDLock createDLock() {
        return new InmemDLock(getActorPath().name());
    }

    /**
     * Acquire the lock for a duration.
     *
     * @return
     */
    protected boolean lock(String lockId, long durationMs) {
        return dlock.lock(lockId, durationMs) == LockResult.SUCCESSFUL;
    }

    /**
     * Release the acquired lock.
     *
     * @param lockId
     * @return
     */
    protected boolean unlock(String lockId) {
        LockResult result = dlock.unlock(lockId);
        return result == LockResult.SUCCESSFUL || result == LockResult.NOT_FOUND;
    }

    /**
     * Get lock's default duration.
     * 
     * @return lock duration in milliseconds
     * @since 0.1.1
     */
    protected long getLockDuration() {
        return dlockTimeMs;
    }

    /**
     * Get the lock object.
     * 
     * @return
     * @since 0.1.1
     */
    protected IDLock getLock() {
        return dlock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initActor() throws Exception {
        if (dlock == null) {
            dlock = createDLock();
        }

        // register message handler
        addMessageHandler(TickMessage.class, this::onTick);

        super.initActor();

        // fire off event for the first time
        if (isRunFirstTimeRegardlessScheduling()) {
            self().tell(new FirstTimeTickMessage(), self());
        }
    }

    /**
     * Get worker's scheduling settings as {@link CronFormat}.
     * 
     * <p>
     * If worker is annotated by {@link Scheduling}, this method returns value
     * of {@link Scheduling#value()}. Otherwise this method throws
     * {@link IllegalStateException}. Sub-class may override this method to
     * customize its own business logic.
     * </p>
     *
     * @return
     */
    protected CronFormat getScheduling() {
        Scheduling[] schedulings = getClass().getAnnotationsByType(Scheduling.class);
        if (schedulings != null && schedulings.length > 0) {
            return CronFormat.parse(schedulings[0].value());
        }
        throw new IllegalStateException(
                "No scheduling defined. Scheduling can be defined via annotation "
                        + Scheduling.class + ", or overriding method getScheduling().");
    }

    /**
     * Sub-class implements this method to actually perform worker business
     * logic.
     *
     * @param lockId
     * @param tick
     * @throws Exception
     */
    protected abstract void doJob(String lockId, TickMessage tick) throws Exception;

    private TickMessage lastTick;

    /**
     * Get last tick this worker had received.
     * 
     * @return
     */
    protected TickMessage getLastTick() {
        return lastTick;
    }

    /**
     * Save last tick this worker had received.
     * 
     * @param tick
     * @return
     */
    protected boolean setLastTick(TickMessage tick) {
        this.lastTick = tick;
        return true;
    }

    /**
     * 30 seconds
     */
    protected final static long DEFAULT_LATE_TICK_THRESHOLD_MS = 30000L;

    /**
     * Sometimes "tick" message comes late. This method returns the maximum
     * amount of time (in milliseconds) the "tick" message can come late.
     * 
     * <p>
     * This method return {@link #DEFAULT_LATE_TICK_THRESHOLD_MS}. Sub-class may
     * override this method to customize its own business logic.
     * </p>
     *
     * @return
     */
    protected long getLateTickThresholdMs() {
        return DEFAULT_LATE_TICK_THRESHOLD_MS;
    }

    /**
     * Check if "tick" matches scheduling settings.
     *
     * @param tick
     * @return
     */
    protected boolean isTickMatched(TickMessage tick) {
        TickMessage lastTick = getLastTick();
        long timestampMs = tick.getTimestamp().getTime();
        if (timestampMs + getLateTickThresholdMs() > System.currentTimeMillis()) {
            // verify if tick is not too old

            long lastTickTimestampMs = lastTick != null ? lastTick.getTimestamp().getTime() : 0;
            if (lastTickTimestampMs == 0 || lastTickTimestampMs < timestampMs) {
                // verify if the received tick is new

                return getScheduling().matches(tick.getTimestamp());
            }
        }
        return false;
    }

    /**
     * If worker is annotated by {@link Scheduling}, this method returns value
     * of {@link Scheduling#getWorkerCoordinationPolicy()}. Otherwise this
     * method returns {@link WorkerCoordinationPolicy#TAKE_ALL_TASKS}. Sub-class
     * may override this method to customize its own business logic.
     * 
     * @return
     * @since 0.1.1
     */
    protected WorkerCoordinationPolicy getWorkerCoordinationPolicy() {
        Scheduling[] schedulings = getClass().getAnnotationsByType(Scheduling.class);
        if (schedulings != null && schedulings.length > 0) {
            return schedulings[0].getWorkerCoordinationPolicy();
        }
        return WorkerCoordinationPolicy.TAKE_ALL_TASKS;
    }

    /**
     * If returns {@code true} a lock will be acquired (see
     * {@link #lock(String, long)}) so that at one given time only one execution
     * of {@link #doJob(TickMessage)} is allowed (same affect as
     * {@code synchronized doJob(TickMessage)}).
     *
     * <p>
     * This method returns {@code true}, sub-class may override this method to
     * fit its own business rule.
     * </p>
     *
     * @return
     * @deprecated since v0.1.1 use {@link #getWorkerCoordinationPolicy()}
     */
    @Deprecated
    protected boolean isRunOnlyWhenNotBusy() {
        throw new RuntimeException(
                "This method is deprecated, use method getWorkerCoordinationPolicy() instead.");
    }

    private Lock localLock = new ReentrantLock(true);

    /**
     * Execute job, local singleton mode, called by
     * {@link #onTick(TickMessage)}.
     * 
     * @param tick
     * @since 0.1.1
     */
    protected void doJobLocalSingleton(TickMessage tick) {
        if (localLock.tryLock()) {
            try {
                doJob(null, tick);
            } catch (Exception e) {
                LOGGER.error("{" + getActorPath() + "} Error while doing job: " + e.getMessage(),
                        e);
            } finally {
                localLock.unlock();
            }
        } else {
            LOGGER.warn("{" + getActorPath() + "} Received TICK message, but I am busy! " + tick);
        }
    }

    /**
     * Execute job, global singleton mode, called by
     * {@link #onTick(TickMessage)}.
     * 
     * Do not forget to release lock. However, delay a short period before
     * releasing lock to avoid the case that {@link #doJob(String, TickMessage)}
     * is do fast that worker instance on another node may receive the very same
     * tick-message and execute the same task.
     * 
     * @param tick
     * @since 0.1.1
     */
    protected void doJobGlobalSingleton(TickMessage tick) {
        String dlockId = AkkaUtils.nextId();
        if (lock(dlockId, getLockDuration())) {
            try {
                doJob(dlockId, tick);
            } catch (Exception e) {
                LOGGER.error("{" + getActorPath() + "} Error while doing job: " + e.getMessage(),
                        e);
            } finally {
                /**
                 * Do not forget to release lock. However, delay a short period
                 * before releasing lock to avoid the case that
                 * {@link #doJob(String, TickMessage)} is so fast that worker
                 * instances on other nodes may receive the very same
                 * tick-message and execute the same task.
                 */
                getActorSystem().scheduler().scheduleOnce(Duration.create(1, TimeUnit.SECONDS),
                        () -> unlock(dlockId),
                        getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS));
            }
        } else {
            LOGGER.warn("{" + getActorPath()
                    + "} Received TICK message, but another instance is taking the task. " + tick);
        }
    }

    /**
     * Execute job, take-all-tasks mode, called by {@link #onTick(TickMessage)}.
     * 
     * @param tick
     * @since 0.1.1
     */
    protected void doJobTakeAllTasks(TickMessage tick) {
        try {
            doJob(null, tick);
        } catch (Exception e) {
            LOGGER.error("{" + getActorPath() + "} Error while doing job: " + e.getMessage(), e);
        }
    }

    /**
     * This method is called when a message of type {@link TickMessage} arrives.
     * 
     * @param tick
     */
    protected void onTick(TickMessage tick) {
        if (isTickMatched(tick) || tick instanceof FirstTimeTickMessage) {
            getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS).execute(() -> {
                setLastTick(tick);

                WorkerCoordinationPolicy wcp = getWorkerCoordinationPolicy();
                switch (wcp) {
                case LOCAL_SINGLETON:
                    doJobLocalSingleton(tick);
                    break;
                case GLOBAL_SINGLETON:
                    doJobGlobalSingleton(tick);
                    break;
                case TAKE_ALL_TASKS:
                    doJobTakeAllTasks(tick);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Received unrecognized worker-coordinator-policy value: " + wcp);
                }
            });
        }
    }

}
