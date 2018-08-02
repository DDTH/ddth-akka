package com.github.ddth.akka.cluster.messages;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.ddth.akka.BaseMessage;
import com.github.ddth.akka.cluster.MasterActor;

import akka.cluster.Member;

/**
 * {@link MasterActor} sends back this message in reply to
 * {@link GetNodesMessage} message.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.4
 */
public class GetNodesResponseMessage extends BaseMessage {
    private static final long serialVersionUID = "0.1.4".hashCode();

    public final String role;
    public final Set<Member> nodes;

    public GetNodesResponseMessage(String replyToId, String role, Set<Member> nodes) {
        setReplyToId(replyToId);
        this.role = role;
        Set<Member> temp = new TreeSet<Member>(new Comparator<Member>() {
            public int compare(Member a, Member b) {
                if (a.isOlderThan(b)) {
                    return -1;
                } else if (b.isOlderThan(a)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        if (nodes != null) {
            temp.addAll(nodes);
        }
        this.nodes = Collections.unmodifiableSet(temp);
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("role", role).append("nodes", nodes).appendSuper(super.toString());
        return tsb.toString();
    }
}
