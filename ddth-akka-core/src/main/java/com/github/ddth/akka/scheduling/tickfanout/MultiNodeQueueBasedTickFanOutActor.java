package com.github.ddth.akka.scheduling.tickfanout;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.TickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.IDLockFactory;
import com.github.ddth.dlock.LockResult;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.IQueueMessage;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

/**
 * Tick fan-out actor that broadcasts "tick" messages to workers in multi-node
 * mode using a queue.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 * @see https://github.com/DDTH/ddth-dlock
 * @see https://github.com/DDTH/ddth-queue
 */
public class MultiNodeQueueBasedTickFanOutActor extends TickFanOutActor {

    private final Logger LOGGER = LoggerFactory.getLogger(MultiNodeQueueBasedTickFanOutActor.class);
    public final static String ACTOR_NAME = AkkaUtils
            .shortenClassName(MultiNodeQueueBasedTickFanOutActor.class);

    /**
     * Helper method to create an instance of
     * {@link MultiNodeQueueBasedTickFanOutActor}.
     * 
     * @param actorSystem
     * @param dlock
     * @param queue
     * @return
     */
    public static ActorRef newInstance(ActorSystem actorSystem, IDLock dlock,
            IQueue<?, byte[]> queue) {
        Props props = Props.create(MultiNodeQueueBasedTickFanOutActor.class, dlock, queue);
        return actorSystem.actorOf(props, ACTOR_NAME);
    }

    /**
     * Helper method to create an instance of
     * {@link MultiNodeQueueBasedTickFanOutActor}.
     * 
     * @param actorSystem
     * @param dlockFactory
     * @param queue
     * @return
     */
    public static ActorRef newInstance(ActorSystem actorSystem, IDLockFactory dlockFactory,
            IQueue<?, byte[]> queue) {
        Props props = Props.create(MultiNodeQueueBasedTickFanOutActor.class, dlockFactory, queue);
        return actorSystem.actorOf(props, ACTOR_NAME);
    }

    /**
     * Marker message to signal {@link MultiNodeQueueBasedTickFanOutActor} that
     * it's time to check queue.
     */
    protected final static class OnQueueTick {
    }

    private final IDLock dlock;
    private final String clientId;
    private final IQueue<Object, byte[]> queue;

    public MultiNodeQueueBasedTickFanOutActor(IDLock dlock, IQueue<Object, byte[]> queue) {
        this.dlock = dlock;
        this.queue = queue;
        this.clientId = AkkaUtils.nextId();
    }

    public MultiNodeQueueBasedTickFanOutActor(IDLockFactory dlockFactory,
            IQueue<Object, byte[]> queue) {
        this.dlock = dlockFactory.createLock(ACTOR_NAME);
        this.queue = queue;
        this.clientId = AkkaUtils.nextId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        super.preStart();
        getTimers().startPeriodicTimer(this.getClass().getSimpleName() + "-queue",
                new OnQueueTick(), Duration.create(1, TimeUnit.MILLISECONDS));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        try {
            getTimers().cancel(this.getClass().getSimpleName() + "-queue");
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        try {
            dlock.unlock(clientId);
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
        if (message instanceof OnQueueTick) {
            getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS).execute(() -> {
                for (TickMessage tickMsg = takeFromQueue(); tickMsg != null; tickMsg = takeFromQueue()) {
                    getContext().system().eventStream().publish(tickMsg);
                }
            });
        } else {
            unhandled(message);
        }
    }

    protected TickMessage takeFromQueue() {
        try {
            IQueueMessage<Object, byte[]> queueMsg = queue.take();
            if (queueMsg != null) {
                queue.finish(queueMsg);
                return TickMessage.fromBytes(queueMsg.qData(), TickMessage.class);
            }
            return null;
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean fanOut(TickMessage tickMessage) {
        if (dlock.lock(clientId, 5000) == LockResult.SUCCESSFUL) {
            IQueueMessage<Object, byte[]> queueMsg = toQueueMessage(tickMessage);
            if (queueMsg != null) {
                boolean status = queue.queue(queueMsg);
                if (!status) {
                    LOGGER.warn("Cannot queue tick message: " + tickMessage);
                }
                return status;
            } else {
                LOGGER.warn("Cannot serialize tick message: " + tickMessage);
                return false;
            }
        }
        return false;
    }

    /**
     * Transform tick message to queue message.
     * 
     * @param tick
     * @return
     */
    protected IQueueMessage<Object, byte[]> toQueueMessage(TickMessage tick) {
        return queue.createMessage(tick.toBytes());
    }

}
