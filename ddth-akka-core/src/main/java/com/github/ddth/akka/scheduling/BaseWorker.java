package com.github.ddth.akka.scheduling;

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.BaseActor;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.inmem.InmemDLock;

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
     * This method return {@code false}, sub-class may override this method to
     * customize its own business logic.
     * </p>
     *
     * @return
     */
    protected boolean isRunFirstTimeRegardlessScheduling() {
        return false;
    }

    private final Collection<Class<?>> channelSubscriptions = Collections
            .singleton(TickMessage.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<Class<?>> channelSubscriptions() {
        return channelSubscriptions;
    }

    private IDLock lock;

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
        return lock.lock(lockId, durationMs) == LockResult.SUCCESSFUL;
    }

    /**
     * Release the acquired lock.
     *
     * @param lockId
     * @return
     */
    protected boolean unlock(String lockId) {
        LockResult result = lock.unlock(lockId);
        return result == LockResult.SUCCESSFUL || result == LockResult.NOT_FOUND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initActor() throws Exception {
        lock = createDLock();

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
     * @param tick
     * @throws Exception
     */
    protected abstract void doJob(TickMessage tick) throws Exception;

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
            long lastTickTimestampMs = lastTick != null ? lastTick.getTimestamp().getTime() : 0;
            // only process if "tick" is not too old
            if (lastTickTimestampMs == 0 || lastTickTimestampMs < timestampMs) {
                return getScheduling().matches(tick.getTimestamp());
            }
        }
        return false;
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
     */
    protected boolean isRunOnlyWhenNotBusy() {
        return true;
    }

    /**
     * This method is called when a message of type {@link TickMessage} arrives.
     * 
     * @param tick
     */
    protected void onTick(TickMessage tick) {
        if (isTickMatched(tick) || tick instanceof FirstTimeTickMessage) {
            getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS).execute(() -> {
                final String lockId = isRunOnlyWhenNotBusy() ? AkkaUtils.nextId() : null;
                if (lockId == null || lock(lockId, 60000)) {
                    try {
                        setLastTick(tick);
                        doJob(tick);
                    } catch (Exception e) {
                        LOGGER.error(
                                "{" + getActorPath() + "} Error while doing job: " + e.getMessage(),
                                e);
                    } finally {
                        if (lockId != null) {
                            if (!unlock(lockId)) {
                                LOGGER.warn("{" + getActorPath() + "} Cannot unlock with lock id ["
                                        + lockId + "]!");
                            }
                        }
                    }
                } else {
                    // Busy processing a previous message
                    LOGGER.warn("{" + getActorPath() + "} Received TICK message, but I am busy! "
                            + tick);
                }
            });
        }
    }

}
