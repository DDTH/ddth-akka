package com.github.ddth.akka.scheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.commons.utils.MapUtils;

import akka.ConfigurationException;
import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.duration.Duration;

/**
 * Actor that sends "tick" messages to all subscribed workers every "tick".
 * 
 * <p>
 * After {@link ActorSystem} is built, create one instance of {@link TickFanOutActor} to broadcast "tick" messages.
 * </p>
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class TickFanOutActor extends AbstractActor {
    /**
     * Marker message to signal {@link TickFanOutActor} that a "tick" has come.
     */
    protected final static class OnTick {
    }

    private final Logger LOGGER = LoggerFactory.getLogger(TickFanOutActor.class);

    private Cancellable clock;
    private long timestampClockStarted = 0;

    /**
     * According to Akka documentation
     * (https://doc.akka.io/docs/akka/2.5/scheduler.html), Akka scheduler is not
     * designed for long-term scheduling. Hence, we renew the clock every 24g,
     * for now.
     * 
     * @since 0.1.3
     */
    protected void renewClock() {
        if (System.currentTimeMillis() - timestampClockStarted > 24L * 3600L * 1000L) {
            stopClock();
            startClock();
            LOGGER.info("Clock renewed.");
        }
    }

    /**
     * Start the "clock" to send "tick" every second.
     * 
     * @since 0.1.3
     */
    protected void startClock() {
        clock = getContext().system().scheduler().schedule(Duration.create(0, TimeUnit.SECONDS),
                Duration.create(1, TimeUnit.SECONDS), () -> {
                    self().tell(new OnTick(), self());
                }, getContext().dispatcher());
        timestampClockStarted = System.currentTimeMillis();
        // getTimers().startPeriodicTimer(this.getClass().getSimpleName(), new
        // OnTick(),
        // Duration.create(1, TimeUnit.SECONDS));
    }

    /**
     * Stop the "clock" that sends "tick" every second.
     * 
     * @since 0.1.3
     */
    protected void stopClock() {
        try {
            if (clock != null) {
                clock.cancel();
            }
            // getTimers().cancel(this.getClass().getSimpleName());
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        } finally {
            clock = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        super.preStart();
        startClock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        stopClock();
        super.postStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder().match(OnTick.class, tick -> {
            fanOut(new TickMessage(MapUtils.createMap("sender", self().path().toString())));
        }).matchAny(this::onReceive).build();
    }

    /**
     * This method is called to handle incoming message.
     * 
     * @param message
     */
    protected void onReceive(Object message) {
        unhandled(message);
    }

    /**
     * Fan-out "tick" message to subscribers.
     *
     * @param tickMsg
     * @return
     */
    protected abstract boolean fanOut(TickMessage tickMsg);

    /**
     * Convenient method to get the associated actor system.
     * 
     * @return
     */
    protected ActorSystem getActorSystem() {
        return context().system();
    }

    protected static Map<String, Boolean> exceptionLoggedGetECE = new HashMap<>();

    /**
     * Get the {@link ExecutionContextExecutor} instance to do async work.
     *
     * @param name
     * @return
     */
    protected ExecutionContextExecutor getExecutionContextExecutor(String name) {
        try {
            return getActorSystem().dispatchers().lookup(name);
        } catch (ConfigurationException e) {
            if (exceptionLoggedGetECE.get(name) == null) {
                LOGGER.warn(e.getMessage());
                exceptionLoggedGetECE.put(name, Boolean.TRUE);
            }
            return getActorSystem().dispatcher();
        }
    }
}
