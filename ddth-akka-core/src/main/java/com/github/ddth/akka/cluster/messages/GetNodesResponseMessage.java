package com.github.ddth.akka.cluster.messages;

import akka.cluster.Member;
import com.github.ddth.akka.BaseMessage;
import com.github.ddth.akka.cluster.MasterActor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

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
        Set<Member> temp = new TreeSet<>((a, b) -> a.isOlderThan(b) ? -1 : (b.isOlderThan(a) ? 1 : 0));
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
