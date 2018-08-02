package com.github.ddth.akka.cluster.messages;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.ddth.akka.BaseMessage;
import com.github.ddth.akka.cluster.MasterActor;

import akka.cluster.Member;

/**
 * Send this message to {@link MasterActor} to ask if a {@link Member} is leader
 * node for a specific role.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.4
 */
public class IsLeaderMessage extends BaseMessage {
    private static final long serialVersionUID = "0.1.4".hashCode();

    public final String role;
    public final Member node;

    public IsLeaderMessage(String role, Member node) {
        this.role = role;
        this.node = node;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("role", role).append("node", node).appendSuper(super.toString());
        return tsb.toString();
    }
}