package com.github.ddth.akka;

import akka.actor.*;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;

/**
 * Akka utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class AkkaUtils {
    /**
     * Create an {@link ActorSystem} with default configurations.
     *
     * <p>Default configurations are loaded from {@code com/github/ddth/akka/default-akka-standalone.conf}</p>
     *
     * @param name
     * @return
     */
    public static ActorSystem createActorSystem(String name) {
        Config config = TypesafeConfigUtils
                .loadConfig(AkkaUtils.class.getClassLoader(), "com/github/ddth/akka/default-akka-standalone.conf",
                        true);
        return ActorSystem.create(name, config);
    }

    /**
     * Create an {@link ActorSystem} with specific configurations.
     *
     * @param name
     * @param config
     * @return
     */
    public static ActorSystem createActorSystem(String name, Config config) {
        return ActorSystem.create(name, config);
    }

    /**
     * Perform a simple ask-response pattern on the target actor ref.
     *
     * @param actorSystem
     * @param target      the target to be asked
     * @param request     the request message to be sent
     * @param timeout     this method may block up to this timeout for response
     * @return
     * @throws TimeoutException thrown if no response within the {@code timeout} window
     * @since 1.1.0
     */
    public static Object simpleAsk(ActorSystem actorSystem, ActorRef target, Object request, Duration timeout)
            throws TimeoutException {
        Inbox inbox = Inbox.create(actorSystem);
        inbox.send(target, request);
        return inbox.receive(timeout);
    }

    /**
     * Find all actors matching a specified path.
     *
     * @param actorSystem
     * @param path
     * @param duration    wait up to this duration while collecting actor ref
     * @return collection of matched actor-ref, empty collection if no actor found
     * @since 1.1.0
     */
    public static Collection<ActorRef> selectActors(ActorSystem actorSystem, ActorPath path, Duration duration) {
        ActorSelection actorSelection = actorSystem.actorSelection(path);
        return selectActors(actorSystem, actorSelection, duration);
    }

    /**
     * Find all actors matching a specified path.
     *
     * @param actorSystem
     * @param path
     * @param duration    wait up to this duration while collecting actor ref
     * @return collection of matched actor-ref, empty collection if no actor found
     * @since 1.1.0
     */
    public static Collection<ActorRef> selectActors(ActorSystem actorSystem, String path, Duration duration) {
        ActorSelection actorSelection = actorSystem.actorSelection(path);
        return selectActors(actorSystem, actorSelection, duration);
    }

    private static Collection<ActorRef> selectActors(ActorSystem actorSystem, ActorSelection actorSelection,
            Duration duration) {
        long waitTimeMs = 10;
        long start = System.currentTimeMillis();
        int counter = 0;
        Collection<ActorRef> result = new HashSet<>();

        String id = com.github.ddth.akka.utils.AkkaUtils.nextId();
        Inbox inbox = Inbox.create(actorSystem);
        actorSelection.tell(new Identify(id), inbox.getRef());
        while (counter < 3) {
            long remainingMs = duration.toMillis() - (System.currentTimeMillis() - start);
            if (remainingMs > 0) {
                Duration remaining = Duration.ofMillis(Math.min(waitTimeMs, remainingMs));
                try {
                    Object msg = inbox.receive(remaining);
                    if (msg instanceof ActorIdentity) {
                        ActorIdentity ai = (ActorIdentity) msg;
                        if (StringUtils.equals(id, ai.correlationId().toString())) {
                            result.add(ai.getRef());
                        }
                    }
                } catch (TimeoutException e) {
                    counter++;
                    waitTimeMs *= 2;
                }
            } else {
                counter = 3;
            }
        }
        return result;
    }
}
