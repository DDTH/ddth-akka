package com.github.ddth.akka.cluster;

import akka.actor.Address;
import akka.cluster.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility class to manage cluster's member nodes.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.3
 */
public class ClusterMemberUtils {
    /**
     * To mark that node/actor has all roles.
     */
    public final static String ROLE_ALL = "*";

    /**
     * Topic to subscribe as "one-actor-per-group"
     */
    public final static String TOPIC_TICK_ONE_PER_GROUP = "TICK";

    /**
     * Topic to subscribe as "all-actors-in-group"
     */
    public final static String TOPIC_TICK_ALL = "TICK-ALL";

    private final static Logger LOGGER = LoggerFactory.getLogger(ClusterMemberUtils.class);

    private final static ConcurrentMap<String, SortedSet<Member>> nodeManager = new ConcurrentHashMap<>();
    private final static ConcurrentMap<Address, Member> members = new ConcurrentHashMap<>();

    /**
     * Reset all nodes info.
     *
     * @since 0.1.4
     */
    synchronized public static void resetNodes() {
        members.clear();
        nodeManager.clear();
    }

    /**
     * Add a member to cluster.
     *
     * @param node
     */
    synchronized public static void addNode(Member node) {
        members.put(node.address(), node);

        Set<String> memberRoles = new HashSet<>(node.getRoles());
        memberRoles.add(ROLE_ALL);
        int counter = 0;
        for (String role : memberRoles) {
            nodeManager.putIfAbsent(role, new TreeSet<>((a, b) -> a.isOlderThan(b) ? -1 : (b.isOlderThan(a) ? 1 : 0)));
            if (nodeManager.get(role).add(node)) {
                counter++;
            }
        }
        LOGGER.info("Node [" + node.address() + "] with roles " + memberRoles + " is UP. Current role repository: "
                + counter + "/" + memberRoles.size() + " role(s).");
    }

    /**
     * Remove a member from cluster.
     *
     * @param node
     */
    synchronized public static void removeNode(Member node) {
        members.remove(node.address());

        Set<String> memberRoles = new HashSet<>(node.getRoles());
        memberRoles.add(ROLE_ALL);
        int counter = 0;
        for (String role : memberRoles) {
            SortedSet<Member> members = nodeManager.get(role);
            if (members != null) {
                if (members.remove(node)) {
                    counter++;
                }
            }
        }
        LOGGER.info("Node [" + node.address() + "] with roles " + memberRoles + " is REMOVED: " + counter + "/"
                + memberRoles.size() + " role(s).");
    }

    /**
     * Check if a node is leader for a role.
     *
     * @param role
     * @param node
     * @return
     */
    public static boolean isLeader(String role, Member node) {
        SortedSet<Member> members = nodeManager.get(role);
        Member leader;
        try {
            leader = members != null ? members.first() : null;
        } catch (NoSuchElementException e) {
            leader = null;
        }
        return leader != null && node.address().equals(leader.address());
    }

    /**
     * Get leader node for a role.
     *
     * @param role
     * @return
     */
    public static Member getLeader(String role) {
        SortedSet<Member> members = nodeManager.get(role);
        try {
            return members != null ? members.first() : null;
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Get all nodes for a role.
     *
     * @param role
     * @return
     * @since 0.1.4
     */
    @SuppressWarnings("unchecked")
    public static Set<Member> getNodes(String role) {
        Set<Member> members = nodeManager.get(role);
        return members != null ? Collections.unmodifiableSet(members) : Collections.EMPTY_SET;
    }
}
