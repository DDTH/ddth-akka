package com.github.ddth.akka.scheduling.tickfanout;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.github.ddth.akka.scheduling.TickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.pubsub.IPubSubHub;
import com.github.ddth.pubsub.ISubscriber;
import com.github.ddth.queue.IMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Tick fan-out actor that broadcasts "tick" messages to workers in multi-node
 * mode using pub/sub.
 *
 * <ul>
 * <li>At one specific time, only one {@link MultiNodePubSubBasedTickFanOutActor} can publish tick-message to pub/sub channel.</li>
 * <li>{@link MultiNodePubSubBasedTickFanOutActor} on all nodes receive the tick-message.</li>
 * <li>Each {@link MultiNodePubSubBasedTickFanOutActor}, then, broadcast the tick-message to local workers.</li>
 * <li>In short: at one specific time, only one {@link MultiNodePubSubBasedTickFanOutActor} creates and publishes tick-message.
 * But the tick-message is received by all workers on all nodes. It's up to the worker to decide the coordination.</li>
 * </ul>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @see {@code https://github.com/DDTH/ddth-dlock}
 * @see {@code https://github.com/DDTH/ddth-queue/blob/master/PUBSUB.md}
 * @since 0.1.1
 */
public class MultiNodePubSubBasedTickFanOutActor extends TickFanOutActor {
    /**
     * Default d-lock time in milliseconds.
     */
    public final static long DEFAULT_DLOCK_TIME_MS = 5000;

    private final Logger LOGGER = LoggerFactory.getLogger(MultiNodePubSubBasedTickFanOutActor.class);
    public final static String ACTOR_NAME = MultiNodePubSubBasedTickFanOutActor.class.getSimpleName();

    /**
     * Helper method to create an instance of
     * {@link MultiNodePubSubBasedTickFanOutActor}.
     *
     * @param actorSystem
     * @param dlock
     * @param dlockTimeMs       d-lock time in milliseconds.
     * @param pubSubHub
     * @param pubSubChannelName
     * @return
     * @since 0.1.1
     */
    public static ActorRef newInstance(ActorSystem actorSystem, IDLock dlock, long dlockTimeMs,
            IPubSubHub<?, byte[]> pubSubHub, String pubSubChannelName) {
        Props props = Props
                .create(MultiNodePubSubBasedTickFanOutActor.class, dlock, dlockTimeMs, pubSubHub, pubSubChannelName);
        return actorSystem.actorOf(props, ACTOR_NAME);
    }

    /**
     * Helper method to create an instance of
     * {@link MultiNodePubSubBasedTickFanOutActor}.
     *
     * @param actorSystem
     * @param dlock
     * @param pubSubHub
     * @param pubSubChannelName
     * @return
     * @since 0.1.1
     */
    public static ActorRef newInstance(ActorSystem actorSystem, IDLock dlock, IPubSubHub<?, byte[]> pubSubHub,
            String pubSubChannelName) {
        return newInstance(actorSystem, dlock, DEFAULT_DLOCK_TIME_MS, pubSubHub, pubSubChannelName);
    }

    private final IDLock dlock;
    private final long dlockTimeMs;
    private final String dlockId;

    private final IPubSubHub<Object, byte[]> pubSubHub;
    private final String pubSubChannelName;

    private ISubscriber<Object, byte[]> subscriber;

    /**
     * Create a new {@link MultiNodePubSubBasedTickFanOutActor} instance.
     *
     * @param dlock
     * @param dlockTimeMs
     * @param pubSubHub
     * @param pubSubChannelName
     * @since 0.1.1
     */
    public MultiNodePubSubBasedTickFanOutActor(IDLock dlock, long dlockTimeMs, IPubSubHub<Object, byte[]> pubSubHub,
            String pubSubChannelName) {
        this.dlock = dlock;
        this.dlockTimeMs = dlockTimeMs;
        this.dlockId = UUID.randomUUID().toString();
        this.pubSubHub = pubSubHub;
        this.pubSubChannelName = pubSubChannelName;
    }

    /**
     * Create a new {@link MultiNodePubSubBasedTickFanOutActor} instance with default d-lock time.
     *
     * @param dlock
     * @param pubSubHub
     * @param pubSubChannelName
     * @since 0.1.1
     */
    public MultiNodePubSubBasedTickFanOutActor(IDLock dlock, IPubSubHub<Object, byte[]> pubSubHub,
            String pubSubChannelName) {
        this(dlock, DEFAULT_DLOCK_TIME_MS, pubSubHub, pubSubChannelName);
    }

    private TickMessage lastTick;

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        super.preStart();

        lastTick = new TickMessage();

        subscriber = (channel, msg) -> {
            TickMessage tickMsg = fromPubSubMessage(msg);
            if (tickMsg != null) {
                if (lastTick != null && !StringUtils.equals(lastTick.getId(), tickMsg.getId())
                        && lastTick.getTimestamp().getTime() <= tickMsg.getTimestamp().getTime()) {
                    lastTick = tickMsg;
                    self().tell(tickMsg, ActorRef.noSender());
                }
            }
            return true;
        };
        pubSubHub.subscribe(pubSubChannelName, subscriber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        try {
            pubSubHub.unsubscribe(pubSubChannelName, subscriber);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        try {
            dlock.unlock(dlockId);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        super.postStop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Object message) {
        if (message instanceof TickMessage) {
            TickMessage tickMsg = (TickMessage) message;
            getContext().system().eventStream().publish(tickMsg);
        } else {
            unhandled(message);
        }
    }

    /**
     * Transform pub/sub message to tick-message.
     *
     * @param msg
     * @return
     */
    protected TickMessage fromPubSubMessage(IMessage<Object, byte[]> msg) {
        try {
            if (msg == null || msg.getId() == null) {
                return null;
            }
            return TickMessage.fromBytes(msg.getData(), TickMessage.class);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Transform tick-message to message for pub/sub.
     *
     * @param tick
     * @return
     */
    protected IMessage<Object, byte[]> toPubSubMessage(TickMessage tick) {
        return pubSubHub.createMessage(tick.toBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean fanOut(TickMessage tickMessage) {
        if (dlock.lock(dlockId, dlockTimeMs) == LockResult.SUCCESSFUL) {
            IMessage<Object, byte[]> msg = toPubSubMessage(tickMessage);
            if (msg != null) {
                boolean status = pubSubHub.publish(pubSubChannelName, msg);
                if (!status) {
                    LOGGER.warn("Cannot publish tick message: " + tickMessage);
                }
                return status;
            } else {
                LOGGER.warn("Cannot serialize tick message: " + tickMessage);
                return false;
            }
        }
        return false;
    }
}
