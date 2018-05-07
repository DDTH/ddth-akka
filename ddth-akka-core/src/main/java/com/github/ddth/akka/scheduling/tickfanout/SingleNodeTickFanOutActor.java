package com.github.ddth.akka.scheduling.tickfanout;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.TickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Tick fan-out actor that broadcasts "tick" messages to local workers, used in
 * single-node mode.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class SingleNodeTickFanOutActor extends TickFanOutActor {

    public final static String ACTOR_NAME = AkkaUtils
            .shortenClassName(SingleNodeTickFanOutActor.class);
    public final static Props PROPS = Props.create(SingleNodeTickFanOutActor.class);

    /**
     * Helper method to create an instance of {@link SingleNodeTickFanOutActor}.
     * 
     * @param actorSystem
     * @return
     */
    public static ActorRef newInstance(ActorSystem actorSystem) {
        return actorSystem.actorOf(PROPS, ACTOR_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean fanOut(TickMessage tickMsg) {
        if (tickMsg != null) {
            getContext().system().eventStream().publish(tickMsg);
            return true;
        }
        return false;
    }

}
