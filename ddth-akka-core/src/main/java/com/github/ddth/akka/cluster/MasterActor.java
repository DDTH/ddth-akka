package com.github.ddth.akka.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;

/**
 * Actor that keeps track of nodes within the cluster.
 * 
 * <p>
 * Create one instance of this actor per node in cluster.
 * </p>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.3
 */
public class MasterActor extends BaseClusterActor {

    public final static String ACTOR_NAME = AkkaUtils.shortenClassName(MasterActor.class);
    public final static Props PROPS = Props.create(MasterActor.class);

    /**
     * Helper method to create an instance of {@link MasterActor}.
     * 
     * @param actorSystem
     * @return
     */
    public static ActorRef newInstance(ActorSystem actorSystem) {
        return actorSystem.actorOf(PROPS, ACTOR_NAME);
    }

    private final Logger LOGGER = LoggerFactory.getLogger(MasterActor.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initActor() throws Exception {
        super.initActor();

        // subscribe to cluster changes
        getCluster().subscribe(self(), ClusterEvent.initialStateAsEvents(), MemberEvent.class,
                UnreachableMember.class);

        // setup message handler
        addMessageHandler(ClusterEvent.MemberUp.class, this::eventMemberUp);
        addMessageHandler(ClusterEvent.MemberRemoved.class, this::eventMemberRemoved);
        addMessageHandler(ClusterEvent.UnreachableMember.class, (msg) -> {
            LOGGER.warn("Node [" + msg.member().address().toString() + "] with roles "
                    + msg.member().getRoles() + " detected as unreachable.");
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void destroyActor() throws Exception {
        try {
            getCluster().unsubscribe(self());
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        super.destroyActor();
    }

    protected void eventMemberUp(MemberUp msg) {
        ClusterMemberUtils.addNode(msg.member());
    }

    protected void eventMemberRemoved(MemberRemoved msg) {
        ClusterMemberUtils.removeNode(msg.member());
    }
}
