package com.github.ddth.akka.cluster;

import java.util.Comparator;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.Address;
import akka.cluster.Member;

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
     * Add a member to cluster.
     * 
     * @param node
     */
    public static void addNode(Member node) {
        members.put(node.address(), node);

        Set<String> memberRoles = new HashSet<>(node.getRoles());
        memberRoles.add(ROLE_ALL);
        int counter = 0;
        for (String role : memberRoles) {
            nodeManager.putIfAbsent(role, new TreeSet<Member>(new Comparator<Member>() {
                public int compare(Member a, Member b) {
                    if (a.isOlderThan(b)) {
                        return -1;
                    } else if (b.isOlderThan(a)) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }));
            if (nodeManager.get(role).add(node)) {
                counter++;
            }
        }
        LOGGER.info("Node [" + node.address() + "] with roles " + memberRoles + " is UP: " + counter
                + "/" + memberRoles.size() + " role(s).");
    }

    /**
     * Remove a member from cluster.
     * 
     * @param node
     */
    public static void removeNode(Member node) {
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
        LOGGER.info("Node [" + node.address() + "] with roles " + memberRoles + " is REMOVED: "
                + counter + "/" + memberRoles.size() + " role(s).");
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
        Member leader = null;
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
        Member leader = null;
        try {
            leader = members != null ? members.first() : null;
        } catch (NoSuchElementException e) {
            leader = null;
        }
        return leader;
    }
}