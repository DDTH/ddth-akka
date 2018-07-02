package com.github.ddth.akka;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.ConfigurationException;
import akka.actor.ActorPath;
import akka.actor.ActorSystem;
import akka.actor.UntypedAbstractActor;
import scala.concurrent.ExecutionContextExecutor;

/**
 * Base class to implement Akka actors.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class BaseActor extends UntypedAbstractActor {

    private final Logger LOGGER = LoggerFactory.getLogger(BaseActor.class);

    protected Map<Class<?>, Consumer<?>> messageHandler = new ConcurrentHashMap<>();

    /**
     * Convenient method to get actor path.
     *
     * @return
     */
    protected ActorPath getActorPath() {
        return self().path();
    }

    /**
     * Convenient method to get the associated actor system.
     * 
     * @return
     */
    protected ActorSystem getActorSystem() {
        return context().system();
    }

    /**
     * Add a message handler. Existing handler will be overridden.
     * 
     * @param clazz
     * @param consumer
     * @return
     */
    protected <T> BaseActor addMessageHandler(Class<T> clazz, Consumer<T> consumer) {
        messageHandler.put(clazz, consumer);
        return this;
    }

    /**
     * Message channels that the actor are subscribed to.
     * 
     * <p>
     * Sub-class overrides this method to supply its own channel list.
     * </p>
     *
     * @return
     */
    protected Collection<Class<?>> channelSubscriptions() {
        return null;
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
            // return the default dispatcher
            return getActorSystem().dispatcher();
        }
    }

    /**
     * Convenient method to perform initializing work.
     *
     * @throws Exception
     */
    protected void initActor() throws Exception {
        // subscribe to message channels
        Collection<Class<?>> msgChannels = channelSubscriptions();
        if (msgChannels != null && msgChannels.size() > 0) {
            msgChannels.forEach(
                    (clazz) -> getContext().system().eventStream().subscribe(self(), clazz));
        }
    }

    /**
     * Mark if this actor has been destroyed.
     */
    protected boolean actorDestroyed = false;

    /**
     * Convenient method to perform cleanup work.
     *
     * @throws Exception
     */
    protected void destroyActor() throws Exception {
        if (!actorDestroyed) {
            // unsubscribe from all message channels
            getContext().system().eventStream().unsubscribe(self());
            actorDestroyed = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        initActor();
        super.preStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        try {
            super.postStop();
        } finally {
            destroyActor();
        }
    }

    /**
     * This method returns
     * {@link MessageHandlerMatchingType#INTERFACE_MATCH_ONLY} by default.
     * 
     * @return
     */
    protected MessageHandlerMatchingType getMessageHandlerMatchingType() {
        return MessageHandlerMatchingType.INTERFACE_MATCH_ONLY;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void onReceive(Object message) {
        if (message == null) {
            return;
        }

        AtomicBoolean handled = new AtomicBoolean(false);
        Class msgClazz = message.getClass();
        MessageHandlerMatchingType mhmt = getMessageHandlerMatchingType();
        if (mhmt == MessageHandlerMatchingType.EXACT_MATCH_ONLY
                || mhmt == MessageHandlerMatchingType.EXACT_MATCH_THEN_INTERFACE) {
            Consumer exactConsumer = messageHandler.get(msgClazz);
            if (exactConsumer != null) {
                // exact match
                handled.set(true);
                getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS)
                        .execute(() -> exactConsumer.accept(message));
            }
        }
        if (!handled.get() && (mhmt == MessageHandlerMatchingType.EXACT_MATCH_THEN_INTERFACE
                || mhmt == MessageHandlerMatchingType.INTERFACE_MATCH_ONLY)) {
            messageHandler.forEach((Class clazz, Consumer consumer) -> {
                // match interface/sub-class
                if (clazz.isAssignableFrom(msgClazz)) {
                    handled.set(true);
                    getExecutionContextExecutor(AkkaUtils.AKKA_DISPATCHER_WORKERS)
                            .execute(() -> consumer.accept(message));
                }
            });
        }

        if (!handled.get()) {
            unhandled(message);
        }
    }

}
