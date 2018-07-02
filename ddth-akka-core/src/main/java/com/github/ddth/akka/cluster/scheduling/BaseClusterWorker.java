package com.github.ddth.akka.cluster.scheduling;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.cluster.BaseClusterActor;
import com.github.ddth.akka.cluster.ClusterMemberUtils;
import com.github.ddth.akka.cluster.DistributedDataUtils.DDGetResult;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.CronFormat;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.WorkerCoordinationPolicy;
import com.github.ddth.akka.scheduling.annotation.Scheduling;

import scala.concurrent.duration.Duration;

/**
 * Base class to implement cluster workers.
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
 * <li>If worker's task is due, {@link #doJob(TickMessage)} is called.
 * {@link #doJob(TickMessage)} then calls {@link #getWorkerCoordinationPolicy()}
 * <ul>
 * <li>If {@link WorkerCoordinationPolicy#TAKE_ALL_TASKS} is returned,
 * {@link #doJobTakeAllTasks(TickMessage)} is called.</li>
 * <li>If {@link WorkerCoordinationPolicy#LOCAL_SINGLETON} is returned,
 * {@link #doJobLocalSingleton(TickMessage)} is called.</li>
 * <li>If {@link WorkerCoordinationPolicy#GLOBAL_SINGLETON} is returned,
 * {@link #doJobGlobalSingleton(TickMessage)} is called.</li>
 * </ul>
 * </li>
 * <li>{@link #doJobTakeAllTasks(TickMessage)},
 * {@link #doJobLocalSingleton(TickMessage)} and
 * {@link #doJobGlobalSingleton(TickMessage)} resolve worker coordinating stuff
 * and finally call {@link #doJob(String, TickMessage)}; sub-class override this
 * method to implement its business logic.</li>
 * </ul>
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.3
 */
public abstract class BaseClusterWorker extends BaseClusterActor {

    /**
     * Special "tick" message to be sent only once when actor starts.
     */
    protected static class FirstTimeTickMessage extends TickMessage {
        private static final long serialVersionUID = "0.1.3".hashCode();
    }

    private Logger LOGGER = LoggerFactory.getLogger(BaseWorker.class);

    public final static long DEFAULT_LOCK_TIME_MS = 10000;
    private long lockTimeMs = DEFAULT_LOCK_TIME_MS;

    public BaseClusterWorker() {
        parseLockTimeFromAnnotation();
    }

    public BaseClusterWorker(long lockTimeMs) {
        this.lockTimeMs = lockTimeMs;
    }

    private void parseLockTimeFromAnnotation() {
        Scheduling[] schedulings = getClass().getAnnotationsByType(Scheduling.class);
        if (schedulings != null && schedulings.length > 0) {
            lockTimeMs = schedulings[0].lockTime();
        }
    }

    private Boolean runFirstTimeRegardlessScheduling;

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
        if (runFirstTimeRegardlessScheduling == null) {
            Scheduling[] schedulings = getClass().getAnnotationsByType(Scheduling.class);
            if (schedulings != null && schedulings.length > 0) {
                runFirstTimeRegardlessScheduling = schedulings[0]
                        .runFirstTimeRegardlessScheduling();
            }
        }
        return runFirstTimeRegardlessScheduling != null
                ? runFirstTimeRegardlessScheduling.booleanValue() : false;
    }

    /**
     * Setter for {@link #runFirstTimeRegardlessScheduling}.
     * 
     * If {@code true}, the first "tick" will fire as soon as the actor starts,
     * ignoring tick-matching check.
     * 
     * @param value
     * @return
     */
    public BaseClusterWorker setRunFirstTimeRegardlessScheduling(boolean value) {
        runFirstTimeRegardlessScheduling = value;
        return this;
    }

    /**
     * Get lock's duration.
     * 
     * @return lock duration in milliseconds
     */
    protected long getLockDuration() {
        return lockTimeMs;
    }

    /**
     * Set lock's duration.
     * 
     * @param duration
     *            lock duration in milliseconds
     * @return
     */
    public BaseClusterWorker setLockDuration(long duration) {
        this.lockTimeMs = duration;
        return this;
    }

    /**
     * This method returns actor's name. Sub-class may override this method to
     * implement its own logic.
     * 
     * @return
     */
    protected String getGroupId() {
        return getActorPath().name();
    }

    private final Collection<String[]> topicSubscriptionAll = Collections
            .singleton(new String[] { ClusterMemberUtils.TOPIC_TICK_ALL });
    private final Collection<String[]> topicSubscriptionOne = Collections
            .singleton(new String[] { ClusterMemberUtils.TOPIC_TICK_ONE_PER_GROUP, getGroupId() });

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String[]> topicSubscriptions() {
        return getWorkerCoordinationPolicy() == WorkerCoordinationPolicy.GLOBAL_SINGLETON
                ? topicSubscriptionOne : topicSubscriptionAll;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initActor() throws Exception {
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
        if (scheduling == null) {
            Scheduling[] schedulings = getClass().getAnnotationsByType(Scheduling.class);
            if (schedulings != null && schedulings.length > 0) {
                scheduling = CronFormat.parse(schedulings[0].value());
            }
        }
        if (scheduling != null) {
            return scheduling;
        }
        throw new IllegalStateException(
                "No scheduling defined. Scheduling can be defined via annotation "
                        + Scheduling.class + ", or overriding method getScheduling().");
    }

    /**
     * Setter for {@link #scheduling}.
     * 
     * @param scheduling
     * @return
     */
    public BaseClusterWorker setScheduling(CronFormat scheduling) {
        this.scheduling = scheduling;
        return this;
    }

    private TickMessage lastTick;

    /**
     * Get last "tick".
     * 
     * @return
     */
    protected TickMessage getLastTick() {
        if (getWorkerCoordinationPolicy() == WorkerCoordinationPolicy.GLOBAL_SINGLETON) {
            DDGetResult getResult = ddGet("last-tick");
            return getResult != null ? getResult.singleValueAs(TickMessage.class) : null;
        }
        return lastTick;
    }

    /**
     * Set last "tick"
     * 
     * @param tick
     */
    protected void setLastTick(TickMessage tick) {
        if (getWorkerCoordinationPolicy() == WorkerCoordinationPolicy.GLOBAL_SINGLETON) {
            ddSet("last-tick", tick);
        } else {
            lastTick = tick;
        }
    }

    /**
     * 30 seconds
     */
    protected final static long DEFAULT_LATE_TICK_THRESHOLD_MS = 30000L;
    private long defaultLateTickThresholdMs = DEFAULT_LATE_TICK_THRESHOLD_MS;

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
        return defaultLateTickThresholdMs;
    }

    /**
     * Setter for {@link #defaultLateTickThresholdMs}.
     * 
     * <p>
     * Sometimes "tick" message comes late. This method returns the maximum
     * amount of time (in milliseconds) the "tick" message can come late.
     * </p>
     * 
     * @param defaultLateTickThresholdMs
     * @return
     */
    public BaseClusterWorker setLateTickThresholdMs(long defaultLateTickThresholdMs) {
        this.defaultLateTickThresholdMs = defaultLateTickThresholdMs;
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
     * If worker is annotated by {@link Scheduling}, this method returns value
     * of {@link Scheduling#workerCoordinationPolicy()}. Otherwise this method
     * returns {@link WorkerCoordinationPolicy#TAKE_ALL_TASKS}. Sub-class may
     * override this method to customize its own business logic.
     * 
     * @return
     */
    protected WorkerCoordinationPolicy getWorkerCoordinationPolicy() {
        if (workerCoordinationPolicy == null) {
            Scheduling[] schedulings = getClass().getAnnotationsByType(Scheduling.class);
            if (schedulings != null && schedulings.length > 0) {
                workerCoordinationPolicy = schedulings[0].workerCoordinationPolicy();
            }
        }
        return workerCoordinationPolicy != null ? workerCoordinationPolicy
                : WorkerCoordinationPolicy.TAKE_ALL_TASKS;
    }

    /**
     * Setter for {@link #workerCoordinationPolicy}.
     * 
     * @param workerCoordinationPolicy
     * @return
     */
    public BaseClusterWorker setWorkerCoordinationPolicy(
            WorkerCoordinationPolicy workerCoordinationPolicy) {
        this.workerCoordinationPolicy = workerCoordinationPolicy;
        return this;
    }

    /**
     * Log a message explaining that the worker receives a task but is unable to
     * execute it because the worker is currently busy.
     * 
     * @param tick
     * @param isGlobal
     */
    protected void logBusy(TickMessage tick, boolean isGlobal) {
        if (isGlobal) {
            LOGGER.warn("{" + getActorPath()
                    + "} Received TICK message, but another instance is taking the task. " + tick);
        } else {
            LOGGER.warn("{" + getActorPath() + "} Received TICK message, but I am busy! " + tick);
        }
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

    private Lock localLock = new ReentrantLock(true);

    /**
     * Execute job, local singleton mode, called by
     * {@link #onTick(TickMessage)}.
     * 
     * @param tick
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
            logBusy(tick, false);
        }
    }

    private String lockKey;

    /**
     * Get lock-key used by {@link #ddLock(String, String, long, TimeUnit)}.
     * 
     * @return
     */
    protected String getLockKey() {
        if (lockKey == null) {
            lockKey = getActorPath().name() + "-lock";
        }
        return lockKey;
    }

    /**
     * Generate a lock-id. Must be unique globally.
     * 
     * @return
     */
    protected String generateLockId() {
        return AkkaUtils.nextId();
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
     */
    protected void doJobGlobalSingleton(TickMessage tick) {
        final String lockId = generateLockId();
        if (ddLock(getLockKey(), lockId, lockTimeMs, TimeUnit.MILLISECONDS)) {
            try {
                doJob(lockId, tick);
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
                        () -> ddUnlock(getLockKey(), lockId),
                        getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS));
            }
        } else {
            logBusy(tick, true);
        }
    }

    /**
     * Execute job, take-all-tasks mode, called by {@link #onTick(TickMessage)}.
     * 
     * @param tick
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
            });
        }
    }
}
