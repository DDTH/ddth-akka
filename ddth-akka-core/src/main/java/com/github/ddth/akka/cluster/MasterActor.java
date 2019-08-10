package com.github.ddth.akka.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import com.github.ddth.akka.cluster.messages.*;
import com.github.ddth.akka.utils.AkkaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConverters;

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

        setHandleMessageAsync(false);

        // subscribe to cluster changes
        getCluster().subscribe(self(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);

        // setup message handlers
        addMessageHandler(ClusterEvent.MemberUp.class, this::eventMemberUp);
        addMessageHandler(ClusterEvent.MemberRemoved.class, this::eventMemberRemoved);
        addMessageHandler(ClusterEvent.UnreachableMember.class, (msg) -> {
            LOGGER.warn("Node [" + msg.member().address().toString() + "] with roles " + msg.member().getRoles()
                    + " detected as unreachable.");
        });

        addMessageHandler(RefreshClusterMembersMessage.class, msg -> {
            ClusterMemberUtils.resetNodes();
            JavaConverters.asJavaIterable(getCluster().state().members()).forEach(m -> {
                LOGGER.warn("Refreshing cluster member " + m);
                if (m.status() == MemberStatus.up()) {
                    ClusterMemberUtils.addNode(m);
                }
            });
        });
        addMessageHandler(GetLeaderMessage.class, msg -> {
            ActorRef sender = sender();
            if (sender != null) {
                Member member = ClusterMemberUtils.getLeader(msg.role);
                sender.tell(new GetLeaderResponseMessage(msg.getId(), msg.role, member), self());
            } else {
                LOGGER.warn("Received message [" + GetLeaderMessage.class.getSimpleName() + "] but sender is null!");
            }
        });
        addMessageHandler(IsLeaderMessage.class, msg -> {
            ActorRef sender = sender();
            if (sender != null) {
                boolean result = ClusterMemberUtils.isLeader(msg.role, msg.node);
                sender.tell(new IsLeaderResponseMessage(msg.getId(), msg.role, msg.node, result), self());
            } else {
                LOGGER.warn("Received message [" + IsLeaderMessage.class.getSimpleName() + "] but sender is null!");
            }
        });
        addMessageHandler(GetNodesMessage.class, msg -> {
            ActorRef sender = sender();
            if (sender != null) {
                sender.tell(new GetNodesResponseMessage(msg.getId(), msg.role, ClusterMemberUtils.getNodes(msg.role)),
                        self());
            } else {
                LOGGER.warn("Received message [" + GetLeaderMessage.class.getSimpleName() + "] but sender is null!");
            }
        });

        // initialize Cluster member state
        self().tell(new RefreshClusterMembersMessage(), self());
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
