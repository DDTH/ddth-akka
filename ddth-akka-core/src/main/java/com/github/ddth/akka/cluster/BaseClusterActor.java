package com.github.ddth.akka.cluster;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.BaseActor;
import com.github.ddth.akka.cluster.DistributedDataUtils.DDGetResult;
import com.github.ddth.akka.cluster.DistributedDataUtils.DDLock;
import com.github.ddth.akka.cluster.DistributedDataUtils.DDTags;
import com.github.ddth.commons.utils.IdGenerator;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.ddata.DistributedData;
import akka.cluster.ddata.Key;
import akka.cluster.ddata.ORMultiMap;
import akka.cluster.ddata.ORMultiMapKey;
import akka.cluster.ddata.Replicator;
import akka.cluster.ddata.Replicator.ReadConsistency;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import scala.concurrent.duration.Duration;

/**
 * Base class to implement Akka cluster actors.
 *
 * <p>
 * Akka actor vs cluster actor:
 * <p>
 *
 * <ul>
 * <li>Management: actors are created & managed by local {@link ActorSystem},
 * cluster actors are created and managed by clustered {@link ActorSystem}.</li>
 * <li>Deployment: actors will be deployed on all nodes, cluster actors can be
 * deployed on a selected group of nodes (nodes with specified roles).</li>
 * <li>Pub-sub: actors subscribe to event channels, cluster actors subscribe to
 * cluster topic.</li>
 * <li>Coordination: actors are independent, cluster nodes can share data via
 * {@link DistributedData}.</li>
 * </ul>
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.3
 */
public class BaseClusterActor extends BaseActor {
    private final Logger LOGGER = LoggerFactory.getLogger(BaseClusterActor.class);

    protected ActorRef distributedPubSubMediator = DistributedPubSub.get(getContext().system())
            .mediator();

    protected ActorRef replicator = DistributedData.get(getContext().getSystem()).replicator();
    protected Key<ORMultiMap<String, Object>> dataKey = ORMultiMapKey.create(getDdKeyId());

    protected long defaultDDGetTimeoutMs = 10000;
    protected Replicator.WriteConsistency writeConsistency = new Replicator.WriteMajority(
            Duration.create(5, TimeUnit.SECONDS));
    protected Replicator.ReadConsistency readConsistency = new Replicator.ReadMajority(
            Duration.create(5, TimeUnit.SECONDS));
    protected Replicator.WriteConsistency lockWriteConsistency = new Replicator.WriteMajority(
            Duration.create(10, TimeUnit.SECONDS));
    protected Replicator.ReadConsistency lockReadConsistency = new Replicator.ReadMajority(
            Duration.create(10, TimeUnit.SECONDS));

    protected Cluster cluster = Cluster.get(getContext().system());

    protected ActorRef getDistributedPubSubMediator() {
        return distributedPubSubMediator;
    }

    protected ActorRef getReplicator() {
        return replicator;
    }

    /**
     * Get {@link Cluster} instance associated with this actor.
     * 
     * @return
     */
    protected Cluster getCluster() {
        return cluster;
    }

    /**
     * Id for distributed-data key. Default value: actor's name.
     * 
     * @return
     */
    protected String getDdKeyId() {
        return getActorPath().name();
    }

    private static boolean containsOrExpires(scala.collection.Set<Object> set,
            DistributedDataUtils.DDLock lock) {
        if (set.contains(lock)) {
            return true;
        }
        for (scala.collection.Iterator<Object> it = set.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof DistributedDataUtils.DDLock) {
                // current lock expired
                DistributedDataUtils.DDLock _lock = (DistributedDataUtils.DDLock) obj;
                if (_lock.expiry < System.currentTimeMillis()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Acquire a lock specified by {@code key}, using Akka's distributed-data
     * APIs.
     *
     * <p>
     * Note: lock is reentrant.
     * </p>
     *
     * <p>
     * Note: This feature is experimental! The lock is considered "weak".
     * </p>
     *
     * @param lockId
     * @return
     */
    protected boolean ddLock(String key, String lockId, long lockTimeout,
            TimeUnit lockTimeoutUnit) {
        DDLock lock = new DDLock(lockId, lockTimeout, lockTimeoutUnit);
        DDTags tags = new DDTags(IdGenerator.getInstance().generateId64(), key);
        Replicator.Update<ORMultiMap<String, Object>> update = new Replicator.Update<>(dataKey,
                ORMultiMap.create(), lockWriteConsistency, Optional.of(tags), curr -> {
                    if (!curr.contains(key) || containsOrExpires(curr.get(key).get(), lock)) {
                        // if lock does not exist or contains myself, or current
                        // lock expires
                        return curr.put(getCluster(), key, Collections.singleton(lock));
                    }
                    return curr;
                });
        replicator.tell(update, self());
        DDGetResult getResult = ddGet(tags, lockReadConsistency, defaultDDGetTimeoutMs,
                TimeUnit.MILLISECONDS);
        boolean result = getResult != null && getResult.valueContains(lock);
        return result;
    }

    /**
     * Release a lock specified by {@code key}, using Akka's distributed-data
     * APIs.
     *
     * <p>
     * Note: This feature is experimental! The lock is considered "weak".
     * </p>
     *
     * @param key
     * @param lockId
     * @return
     */
    protected boolean ddUnlock(String key, String lockId) {
        DDLock lock = new DDLock(lockId);
        DDTags tags = new DDTags(IdGenerator.getInstance().generateId64(), key);
        Replicator.Update<ORMultiMap<String, Object>> update = new Replicator.Update<>(dataKey,
                ORMultiMap.create(), lockWriteConsistency, Optional.of(tags),
                curr -> curr.contains(key) && containsOrExpires(curr.get(key).get(), lock)
                        ? curr.remove(getCluster(), key) : curr);
        replicator.tell(update, self());
        DDGetResult getResult = ddGet(tags, lockReadConsistency, defaultDDGetTimeoutMs,
                TimeUnit.MILLISECONDS);
        boolean result = getResult != null && (getResult.isNotFound() || getResult.isNullOrEmpty());
        return result;
    }

    /**
     * Delete a distributed-data record specified by {@code tags}.
     *
     * @param tags
     */
    protected void ddDelete(DDTags tags) {
        replicator.tell(
                new Replicator.Update<>(dataKey, ORMultiMap.create(), writeConsistency,
                        Optional.of(tags), curr -> curr.remove(getCluster(), tags.getKey())),
                self());
    }

    /**
     * Delete a distributed-data record specified by {@code key}.
     *
     * @param key
     */
    protected void ddDelete(String key) {
        ddDelete(new DDTags(IdGenerator.getInstance().generateId64(), key));
    }

    /**
     * Set a distributed-data record.
     *
     * @param tags
     * @param value
     */
    protected void ddSet(DDTags tags, Object value) {
        replicator.tell(
                new Replicator.Update<>(dataKey,
                        ORMultiMap.create(), writeConsistency, Optional.of(tags), curr -> curr
                                .put(getCluster(), tags.getKey(), Collections.singleton(value))),
                self());
    }

    /**
     * Set a distributed-data record.
     *
     * @param key
     * @param value
     */
    protected void ddSet(String key, Object value) {
        ddSet(new DDTags(IdGenerator.getInstance().generateId64(), key), value);
    }

    /**
     * Get a distributed-data record specified by {@code key}.
     *
     * @param key
     * @return
     */
    protected DDGetResult ddGet(String key) {
        return ddGet(key, defaultDDGetTimeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Get a distributed-data record specified by {@code key}.
     *
     * @param key
     * @param timeout
     * @param timeoutUnit
     * @return
     */
    protected DDGetResult ddGet(String key, long timeout, TimeUnit timeoutUnit) {
        return ddGet(key, timeout, timeoutUnit, readConsistency);
    }

    /**
     * Get a distributed-data record specified by {@code key}.
     *
     * @param key
     * @param timeout
     * @param timeoutUnit
     * @param readConsistency
     * @return
     * @since template-v2.6.r7
     */
    protected DDGetResult ddGet(String key, long timeout, TimeUnit timeoutUnit,
            ReadConsistency readConsistency) {
        return ddGet(new DDTags(IdGenerator.getInstance().generateId64(), key), readConsistency,
                timeout, timeoutUnit);
    }

    /**
     * Get a distributed-data record.
     *
     * @param tags
     * @param readConsistency
     * @param timeout
     * @param timeoutUnit
     * @return
     */
    protected DDGetResult ddGet(DDTags tags, ReadConsistency readConsistency, long timeout,
            TimeUnit timeoutUnit) {
        replicator.tell(new Replicator.Get<>(dataKey, readConsistency, Optional.of(tags)), self());
        long expiry = System.currentTimeMillis() + timeoutUnit.toMillis(timeout);
        DDGetResult result = DistributedDataUtils.getResponse(tags.getId());
        while (result == null && System.currentTimeMillis() <= expiry) {
            LockSupport.parkNanos(1000);
            result = DistributedDataUtils.getResponse(tags.getId());
        }
        return result != null ? result : null;
    }

    /**
     * Roles of nodes that the actor is deployed.
     *
     * <p>
     * {@code null} or empty result, or result contains
     * {@link ClusterMemberUtils#ROLE_ALL} means the actor is to be deployed on
     * all cluster nodes.
     * </p>
     *
     * @return
     */
    protected Set<String> getDeployRoles() {
        return null;
    }

    /**
     * Topics (and group-id) that the actor is subscribed to.
     *
     * <ul>
     * <li>First entry of {@code String[]} is topic name, the second one is
     * group-id.</li>
     * <li>The second entry can be omitted. If so, group-id is
     * {@code null}.</li>
     * <li>Actor can subscribe to one topic twice.</li>
     * </ul>
     *
     * @return
     */
    protected Collection<String[]> topicSubscriptions() {
        return null;
    }

    /**
     * Convenient method to perform initializing work.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected void initActor() throws Exception {
        Set<String> selfRoles = cluster.getSelfRoles();
        Set<String> deployRoles = getDeployRoles();
        if (deployRoles == null || deployRoles.isEmpty()
                || deployRoles.contains(ClusterMemberUtils.ROLE_ALL)
                || !Collections.disjoint(deployRoles, selfRoles)) {
            super.initActor();

            addMessageHandler(DistributedPubSubMediator.SubscribeAck.class,
                    ack -> LOGGER.info("{" + getActorPath().name()
                            + "} subscribed successfully to [" + ack.subscribe() + "]."));
            addMessageHandler(DistributedPubSubMediator.UnsubscribeAck.class,
                    ack -> LOGGER.info("{" + getActorPath().name()
                            + "} unsubscribed successfully from [" + ack.unsubscribe() + "]."));

            // /*
            // * distributed-data delete
            // */
            // addMessageHandler(Replicator.DeleteResponse.class, msg -> {
            // // event includes DeleteSuccess, DataDeleted, Deleted and
            // // ReplicationDeleteFailure
            // Object _obj = msg.getRequest().orElseGet(null);
            // DDTags tags = _obj instanceof DDTags ? (DDTags) _obj :
            // DDTags.EMPTY;
            // DistributedDataUtils.setResponse(tags.getId(), null);
            // });
            //
            // /*
            // * distributed-data update
            // */
            // addMessageHandler(Replicator.UpdateSuccess.class, msg -> {
            // Object _obj = msg.getRequest().orElse(null);
            // DDTags tags = _obj instanceof DDTags ? (DDTags) _obj :
            // DDTags.EMPTY;
            // DistributedDataUtils.setResponse(tags.getId(),
            // DDGetResult.notFound(tags));
            // });

            /*
             * distributed-data get
             */
            addMessageHandler(Replicator.GetFailure.class, msg -> {
                Object _obj = msg.getRequest().orElse(null);
                DDTags tags = _obj instanceof DDTags ? (DDTags) _obj : null;
                if (tags != null) {
                    DistributedDataUtils.setResponse(tags.getId(), DDGetResult.error(tags));
                }
            });
            addMessageHandler(Replicator.NotFound.class, msg -> {
                Object _obj = msg.getRequest().orElse(null);
                DDTags tags = _obj instanceof DDTags ? (DDTags) _obj : null;
                if (tags != null) {
                    DistributedDataUtils.setResponse(tags.getId(), DDGetResult.notFound(tags));
                }
            });
            addMessageHandler(Replicator.GetSuccess.class, msg -> {
                Object _tags = msg.getRequest().orElse(null);
                DDTags tags = _tags instanceof DDTags ? (DDTags) _tags : null;
                if (tags != null) {
                    ORMultiMap<String, Object> data = msg.dataValue() instanceof ORMultiMap
                            ? (ORMultiMap<String, Object>) msg.dataValue() : ORMultiMap.create();
                    Set<Object> value = data.getEntries().get(tags.getKey());
                    DistributedDataUtils.setResponse(tags.getId(), DDGetResult.ok(tags, value));
                }
            });

            Collection<String[]> topicSubscriptions = topicSubscriptions();
            if (topicSubscriptions != null) {
                topicSubscriptions.forEach((topicSub) -> {
                    String topic = topicSub != null && topicSub.length > 0 ? topicSub[0] : null;
                    String groupId = topicSub != null && topicSub.length > 1 ? topicSub[1] : null;
                    subscribeToTopic(topic, groupId);
                });
            }
        } else {
            LOGGER.info("Actor {" + getActorPath().name()
                    + "} is configured to start on node with roles " + deployRoles
                    + " but this node " + cluster.selfAddress() + " has roles " + selfRoles);
            getContext().stop(self());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void destroyActor() throws Exception {
        try {
            Collection<String[]> topicSubscriptions = topicSubscriptions();
            if (topicSubscriptions != null) {
                topicSubscriptions.forEach((topicSub) -> {
                    String topic = topicSub != null && topicSub.length > 0 ? topicSub[0] : null;
                    String groupId = topicSub != null && topicSub.length > 1 ? topicSub[1] : null;
                    unsubscribeFromTopic(topic, groupId);
                });
            }
        } catch (Exception e) {
            LOGGER.warn("{" + getActorPath().name() + "} " + e.getMessage(), e);
        }
        super.destroyActor();
    }

    /**
     * Publish a message to a topic.
     *
     * <p>
     * If {@code sendOneMessageToEachGroup=true}, each message published to the
     * topic is delivered via the supplied {@code RoutingLogic} (default random)
     * to one actor within each subscribing group.
     * </p>
     *
     * <p>
     * Note that if the group id is used it is part of the topic identifier.
     * Messages published with {@code sendOneMessageToEachGroup=false} will not
     * be delivered to subscribers that subscribed with a group id. Messages
     * published with {@code sendOneMessageToEachGroup=true} will not be
     * delivered to subscribers that subscribed without a group id.
     * </p>
     *
     * @param message
     * @param topic
     * @param sendOneMessageToEachGroup
     */
    protected void publishToTopic(Object message, String topic, boolean sendOneMessageToEachGroup) {
        distributedPubSubMediator.tell(
                new DistributedPubSubMediator.Publish(topic, message, sendOneMessageToEachGroup),
                self());
    }

    /**
     * Subscribe to a topic, without a group-id.
     *
     * @param topic
     */
    protected void subscribeToTopic(String topic) {
        subscribeToTopic(topic, null);
    }

    /**
     * Subscribe to a topic, as a group-id.
     *
     * @param topic
     * @param groupId
     */
    protected void subscribeToTopic(String topic, String groupId) {
        if (!StringUtils.isBlank(topic)) {
            if (StringUtils.isBlank(groupId)) {
                distributedPubSubMediator
                        .tell(new DistributedPubSubMediator.Subscribe(topic, self()), self());
                LOGGER.info(
                        "{" + getActorPath().name() + "} is subscribing to topic [" + topic + "].");
            } else {
                distributedPubSubMediator.tell(
                        new DistributedPubSubMediator.Subscribe(topic, groupId, self()), self());
                LOGGER.info("{" + getActorPath().name() + "} is subscribing to topic [" + topic
                        + "] as [" + groupId + "].");
            }
        }
    }

    /**
     * Unsubscribe from a topic, without a group-id.
     *
     * @param topic
     */
    protected void unsubscribeFromTopic(String topic) {
        unsubscribeFromTopic(topic, null);
    }

    /**
     * Unsubscribe from a topic, as a group-id.
     *
     * @param topic
     * @param groupId
     */
    protected void unsubscribeFromTopic(String topic, String groupId) {
        if (StringUtils.isBlank(groupId)) {
            distributedPubSubMediator.tell(new DistributedPubSubMediator.Unsubscribe(topic, self()),
                    self());
            LOGGER.info(
                    "{" + getActorPath().name() + "} is unsubscribing from topic [" + topic + "].");
        } else {
            distributedPubSubMediator.tell(
                    new DistributedPubSubMediator.Unsubscribe(topic, groupId, self()), self());
            LOGGER.info("{" + getActorPath().name() + "} is unsubscribing from topic [" + topic
                    + "] as [" + groupId + "].");
        }
    }
}
