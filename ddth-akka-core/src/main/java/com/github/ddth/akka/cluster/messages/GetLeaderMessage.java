package com.github.ddth.akka.cluster.messages;

import com.github.ddth.akka.BaseMessage;
import com.github.ddth.akka.cluster.MasterActor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Send this message to {@link MasterActor} to ask who currently has the role leader.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.4
 */
public class GetLeaderMessage extends BaseMessage {
    private static final long serialVersionUID = "0.1.4".hashCode();

    public final String role;

    public GetLeaderMessage(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("role", role).appendSuper(super.toString());
        return tsb.toString();
    }
}
