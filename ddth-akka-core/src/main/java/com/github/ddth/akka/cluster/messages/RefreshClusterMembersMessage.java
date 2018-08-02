package com.github.ddth.akka.cluster.messages;

import com.github.ddth.akka.BaseMessage;
import com.github.ddth.akka.cluster.MasterActor;

/**
 * Send this message to {@link MasterActor} to refresh cluster's member state.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.4
 */
public class RefreshClusterMembersMessage extends BaseMessage {
    private static final long serialVersionUID = "0.1.4".hashCode();
}
