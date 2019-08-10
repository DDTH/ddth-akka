package com.github.ddth.akka.scheduling;

import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import com.github.ddth.akka.BaseActor;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.akka.utils.AkkaUtils;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.dlock.impl.inmem.InmemDLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class to implement workers.
 *
 * <p>
 * Worker implementation:
 * <ul>
 * <li>Worker is scheduled to perform task. Scheduling configuration is in Cron-like format (see {@link CronFormat} and {@link #getScheduling()}).</li>
 * <li>At every "tick", worker receives a "tick" message (see {@link TickMessage}). The "tick" message carries a timestamp and a unique id.
 * This timestamp is checked against worker's scheduling configuration to determine that worker's task should be fired off or not.</li>
 * <li>If worker's task is due, {@link #onTick(TickMessage)} is called. {@link #onTick(TickMessage)} then calls {@link #getWorkerCoordinationPolicy()}
 * <ul>
 * <li>If {@link WorkerCoordinationPolicy#TAKE_ALL_TASKS} is returned, {@link #doJobTakeAllTasks(TickMessage)} is called.</li>
 * <li>If {@link WorkerCoordinationPolicy#LOCAL_SINGLETON} is returned, {@link #doJobLocalSingleton(TickMessage)} is called.</li>
 * <li>If {@link WorkerCoordinationPolicy#GLOBAL_SINGLETON} is returned, {@link #doJobGlobalSingleton(TickMessage)} is called.</li>
 * </ul>
 * </li>
 * <li>{@link #doJobTakeAllTasks(TickMessage)}, {@link #doJobLocalSingleton(TickMessage)} and {@link #doJobGlobalSingleton(TickMessage)} resolve worker coordinating stuff
 * and finally call {@link #doJob(String, TickMessage)}; sub-class override this method to implement its business logic.</li>
 * </ul>
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class BaseWorker extends BaseActor {
    /**
     * Special "tick" message to be sent only once when actor starts.
     */
    protected static class FirstTimeTickMessage extends TickMessage {
        private static final long serialVersionUID = "0.1.0".hashCode();
    }

    private Logger LOGGER = LoggerFactory.getLogger(BaseWorker.class);

    public final static long DEFAULT_DLOCK_TIME_MS = 10000;
    private long dlockTimeMs = DEFAULT_DLOCK_TIME_MS;
    private IDLock dlock;

    private final static Collection<Class<?>> channelSubscriptions = Collections.singleton(TickMessage.class);

    public BaseWorker() {
        parseAnnotation();
        parseDlockTimeFromAnnotation();
    }

    public BaseWorker(IDLock dlock) {
        this.dlock = dlock;
        parseAnnotation();
        parseDlockTimeFromAnnotation();
    }

    public BaseWorker(IDLock dlock, long dlockTimeMs) {
        this.dlock = dlock;
        this.dlockTimeMs = dlockTimeMs;
        parseAnnotation();
    }

    /**
     * @since 1.0.0
     */
    protected void parseAnnotation() {
        Scheduling[] schedulings = getClass().getAnnotationsByType(Scheduling.class);
        annotatedScheduling = schedulings != null && schedulings.length > 0 ? schedulings[0] : null;
    }

    /**
     * @return
     * @since 1.0.0
     */
    protected Scheduling getAnnotatedScheduling() {
        return annotatedScheduling;
    }

    /**
     * @since 0.1.2
     */
    private void parseDlockTimeFromAnnotation() {
        if (annotatedScheduling != null) {
            dlockTimeMs = annotatedScheduling.lockTime();
        }
    }

    private Boolean runFirstTimeRegardlessScheduling;
    private Scheduling annotatedScheduling;

    /**
     * If {@code true}, the first "tick" will fire off task-executing as soon as the actor starts,
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
        if (runFirstTimeRegardlessScheduling == null && annotatedScheduling != null) {
            setRunFirstTimeRegardlessScheduling(annotatedScheduling.runFirstTimeRegardlessScheduling());
        }
        return runFirstTimeRegardlessScheduling != null ? runFirstTimeRegardlessScheduling.booleanValue() : false;
    }

    /**
     * If {@code true}, the first "tick" will fire off task-executing as soon as the actor starts,
     * ignoring tick-matching check.
     *
     * @param value
     * @return
     */
    public BaseWorker setRunFirstTimeRegardlessScheduling(boolean value) {
        runFirstTimeRegardlessScheduling = value;
        return this;
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
     * @param lockId
     * @param durationMs
     * @return
     */
    protected boolean lock(String lockId, long durationMs) {
        return dlock.lock(lockId, durationMs) == LockResult.SUCCESSFUL;
    }

    /**
     * Acquire the lock for a duration.
     *
     * @param lockWaitWeight
     * @param lockId
     * @param durationMs
     * @return
     * @since 0.1.2
     */
    protected boolean lock(int lockWaitWeight, String lockId, long durationMs) {
        return dlock.lock(lockWaitWeight, lockId, durationMs) == LockResult.SUCCESSFUL;
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
     * Get lock's duration.
     *
     * @return lock duration in milliseconds
     * @since 0.1.1
     */
    protected long getLockDuration() {
        return dlockTimeMs;
    }

    /**
     * Set lock's duration.
     *
     * @param duration lock duration in milliseconds
     * @return
     * @since 0.1.2
     */
    public BaseWorker setLockDuration(long duration) {
        this.dlockTimeMs = duration;
        return this;
    }

    /**
     * Distributed-lock instance associated with this worker.
     *
     * @return
     * @since 0.1.1
     */
    protected IDLock getLock() {
        return dlock;
    }

    /**
     * Distributed-lock instance associated with this worker.
     *
     * @param lock
     * @return
     * @since 0.1.2
     */
    public BaseWorker setLock(IDLock lock) {
        this.dlock = lock;
        return this;
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

    private CronFormat scheduling;

    /**
     * Worker's scheduling settings as {@link CronFormat}.
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
        if (scheduling == null) {
            scheduling = annotatedScheduling != null ? CronFormat.parse(annotatedScheduling.value()) : null;
        }
        if (scheduling != null) {
            return scheduling;
        }
        throw new IllegalStateException(
                "No scheduling defined. Scheduling can be defined via annotation " + Scheduling.class
                        + ", or overriding method getScheduling().");
    }

    /**
     * Worker's scheduling settings as {@link CronFormat}.
     *
     * @param scheduling
     * @return
     */
    public BaseWorker setScheduling(CronFormat scheduling) {
        this.scheduling = scheduling;
        return this;
    }

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
     */
    protected void setLastTick(TickMessage tick) {
        this.lastTick = tick;
    }

    /**
     * 30 seconds
     */
    protected final static long DEFAULT_LATE_TICK_THRESHOLD_MS = 30000L;
    private long lateTickThresholdMs = DEFAULT_LATE_TICK_THRESHOLD_MS;

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
        return lateTickThresholdMs;
    }

    /**
     * Sometimes "tick" message comes late. This method sets the maximum
     * amount of time (in milliseconds) the "tick" message can come late.
     *
     * @param defaultLateTickThresholdMs
     * @return
     */
    public BaseWorker setLateTickThresholdMs(long defaultLateTickThresholdMs) {
        this.lateTickThresholdMs = defaultLateTickThresholdMs;
        return this;
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

    private WorkerCoordinationPolicy workerCoordinationPolicy;

    /**
     * Specify how workers are coordinated.
     *
     * <p>If worker is annotated by {@link Scheduling}, this method returns value
     * of {@link Scheduling#workerCoordinationPolicy()}. Otherwise this method
     * returns {@link WorkerCoordinationPolicy#TAKE_ALL_TASKS}. Sub-class may
     * override this method to customize its own business logic.</p>
     *
     * @return
     * @since 0.1.1
     */
    protected WorkerCoordinationPolicy getWorkerCoordinationPolicy() {
        if (workerCoordinationPolicy == null) {
            workerCoordinationPolicy =
                    annotatedScheduling != null ? annotatedScheduling.workerCoordinationPolicy() : null;
        }
        return workerCoordinationPolicy != null ? workerCoordinationPolicy : WorkerCoordinationPolicy.TAKE_ALL_TASKS;
    }

    /**
     * Specify how workers are coordinated.
     *
     * @param workerCoordinationPolicy
     * @return
     * @since 0.1.2
     */
    public BaseWorker setWorkerCoordinationPolicy(WorkerCoordinationPolicy workerCoordinationPolicy) {
        this.workerCoordinationPolicy = workerCoordinationPolicy;
        return this;
    }

    /**
     * Log a message explaining that the worker receives a task but is unable to
     * execute it because the worker is currently busy.
     *
     * @param tick
     * @param isGlobal
     * @since 0.1.1.2
     */
    protected void logBusy(TickMessage tick, boolean isGlobal) {
        if (isGlobal) {
            LOGGER.warn(
                    "{" + getActorPath() + "} Received TICK message, but another instance is taking the task. " + tick);
        } else {
            LOGGER.warn("{" + getActorPath() + "} Received TICK message, but I am busy! " + tick);
        }
    }

    /**
     * Sub-class implements this method to actually perform worker's business logic.
     *
     * @param lockId
     * @param tick
     * @throws Exception
     */
    protected abstract void doJob(String lockId, TickMessage tick) throws Exception;

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
                LOGGER.error("{" + getActorPath() + "} Error while doing job: " + e.getMessage(), e);
            } finally {
                localLock.unlock();
            }
        } else {
            logBusy(tick, false);
        }
    }

    /**
     * Generate a dlock-id. Must be unique globally.
     *
     * @return
     * @since 0.1.1.1
     */
    protected String generateDLockId() {
        return AkkaUtils.nextId();
    }

    private Boolean lockFairness;

    /**
     * If {@code true}, try to lock with fairness, if possible.
     *
     * @return
     * @since 0.1.2
     */
    protected boolean isLockFairness() {
        if (lockFairness == null && annotatedScheduling != null) {
            setLockFairness(annotatedScheduling.lockFairness());
        }
        return lockFairness != null ? lockFairness.booleanValue() : false;
    }

    /**
     * If {@code true}, try to lock with fairness, if possible.
     *
     * @param lockFairness
     * @return
     * @since 0.1.2
     */
    public BaseWorker setLockFairness(boolean lockFairness) {
        this.lockFairness = lockFairness ? Boolean.TRUE : Boolean.FALSE;
        return this;
    }

    private int dlockWait = 0;

    /**
     * Execute job, global singleton mode, called by {@link #onTick(TickMessage)}.
     *
     * <p>
     * Do not forget to release lock. However, delay a short period before
     * releasing lock to avoid the case that {@link #doJob(String, TickMessage)}
     * is do fast that worker instance on another node may receive the very same
     * tick-message and execute the same task.
     * </p>
     *
     * @param tick
     * @since 0.1.1
     */
    protected void doJobGlobalSingleton(TickMessage tick) {
        String dlockId = generateDLockId();
        if (isLockFairness() ? lock(dlockWait, dlockId, getLockDuration()) : lock(dlockId, getLockDuration())) {
            try {
                dlockWait = 0;
                doJob(dlockId, tick);
            } catch (Exception e) {
                LOGGER.error("{" + getActorPath() + "} Error while doing job: " + e.getMessage(), e);
            } finally {
                /**
                 * Do not forget to release lock. However, delay a short period
                 * before releasing lock to avoid the case that
                 * {@link #doJob(String, TickMessage)} is so fast that worker
                 * instances on other nodes may receive the very same
                 * tick-message and execute the same task.
                 */
                ActorSystem actorSystem = getActorSystem();
                Scheduler scheduler = actorSystem != null ? actorSystem.scheduler() : null;
                if (scheduler != null) {
                    scheduler.scheduleOnce(Duration.create(1, TimeUnit.SECONDS), () -> unlock(dlockId),
                            getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS));
                } else {
                    LOGGER.warn("Cannot obtain a Scheduler from ActorSystem.");
                    unlock(dlockId);
                }
            }
        } else {
            dlockWait++;
            logBusy(tick, true);
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

    private void _onTick(TickMessage tick) {
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
            LOGGER.error("Received unrecognized worker-coordinator-policy value: " + wcp);
        }
        setLastTick(tick);
    }

    /**
     * This method is called when a message of type {@link TickMessage} arrives.
     *
     * @param tick
     */
    protected void onTick(TickMessage tick) {
        if (isTickMatched(tick) || tick instanceof FirstTimeTickMessage) {
            if (handleMessageAsync) {
                getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS).execute(() -> _onTick(tick));
            } else {
                _onTick(tick);
            }
        }
    }
}
