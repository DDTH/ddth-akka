package com.github.ddth.akka.cluster.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.cluster.ClusterMemberUtils;
import com.github.ddth.akka.scheduling.TickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.tickfanout.SingleNodeTickFanOutActor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;

/**
 * Cluster fan-out "tick" messages only if the current node is leader
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.3
 */
public class ClusterTickFanOutActor extends TickFanOutActor {

    public final static String ACTOR_NAME = AkkaUtils.shortenClassName(ClusterTickFanOutActor.class);
    public final static Props PROPS = Props.create(ClusterTickFanOutActor.class);

    /**
     * Helper method to create an instance of {@link ClusterTickFanOutActor}.
     * 
     * @param actorSystem
     * @return
     */
    public static ActorRef newInstance(ActorSystem actorSystem) {
        return actorSystem.actorOf(PROPS, ACTOR_NAME);
    }

    private final Logger LOGGER = LoggerFactory.getLogger(ClusterTickFanOutActor.class);

    private ActorRef distributedPubSubMediator = DistributedPubSub.get(getContext().system())
            .mediator();
    private Cluster cluster = Cluster.get(getContext().system());

    private void publishToTopic(Object message, String topic, boolean sendOneMessageToEachGroup) {
        distributedPubSubMediator.tell(
                new DistributedPubSubMediator.Publish(topic, message, sendOneMessageToEachGroup),
                self());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean fanOut(TickMessage tickMsg) {
        final String CLUSTER_GROUP = ClusterMemberUtils.ROLE_ALL;

        Member leader = ClusterMemberUtils.getLeader(CLUSTER_GROUP);
        if (leader == null) {
            LOGGER.warn("Received TICK message, but cluster group [" + CLUSTER_GROUP
                    + "] is empty! " + tickMsg);
        } else {
            Address thisNodeAddr = cluster.selfAddress();
            if (thisNodeAddr.equals(leader.address())) {
                publishToTopic(tickMsg, ClusterMemberUtils.TOPIC_TICK_ONE_PER_GROUP, true);
                publishToTopic(tickMsg, ClusterMemberUtils.TOPIC_TICK_ALL, false);
            } else {
                // I am not leader!
            }
        }
        return false;
    }

}
