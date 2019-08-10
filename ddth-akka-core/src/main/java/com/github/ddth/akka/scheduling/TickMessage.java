package com.github.ddth.akka.scheduling;

import com.github.ddth.akka.BaseMessage;

import java.util.Map;

/**
 * A message that encapsulates a "tick".
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class TickMessage extends BaseMessage {
    private static final long serialVersionUID = "0.1.0".hashCode();

    public TickMessage() {
    }

    public TickMessage(Map<String, Object> tags) {
        super(tags);
    }

    public TickMessage(String id) {
        super(id);
    }

    public TickMessage(String id, String replyToId) {
        super(id, replyToId);
    }

    public TickMessage(String id, Map<String, Object> tags) {
        super(id, tags);
    }

    public TickMessage(String id, String replyToId, Map<String, Object> tags) {
        super(id, replyToId, tags);
    }
}
