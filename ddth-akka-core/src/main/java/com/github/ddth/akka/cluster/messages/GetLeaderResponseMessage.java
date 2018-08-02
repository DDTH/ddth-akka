package com.github.ddth.akka.cluster.messages;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.ddth.akka.BaseMessage;
import com.github.ddth.akka.cluster.MasterActor;

import akka.cluster.Member;

/**
 * {@link MasterActor} sends back this message in reply to
 * {@link GetLeaderMessage} message.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.4
 */
public class GetLeaderResponseMessage extends BaseMessage {
    private static final long serialVersionUID = "0.1.4".hashCode();

    public final String role;
    public final Member node;

    public GetLeaderResponseMessage(String replyToId, String role, Member node) {
        setReplyToId(replyToId);
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