package com.github.ddth.akka;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.commons.utils.SerializationUtils;

/**
 * Base class to implement Akka's messages.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.4
 */
public class BaseMessage implements Serializable {

    private static final long serialVersionUID = "0.1.4".hashCode();

    /**
     * Message's unique id.
     */
    private final String id;

    /**
     * Id of the message this message is replying to.
     */
    private String replyToId;

    /**
     * Message's timestamp, when the message was created.
     */
    private final Date timestamp = new Date();

    private final Map<String, Object> tags = new HashMap<>();

    public BaseMessage() {
        id = AkkaUtils.nextId();
        replyToId = null;
    }

    public BaseMessage(Map<String, Object> tags) {
        this();
        if (tags != null) {
            this.tags.putAll(tags);
        }
    }

    public BaseMessage(String id) {
        this.id = id;
        replyToId = null;
    }

    public BaseMessage(String id, String replyToId) {
        this.id = id;
        this.replyToId = replyToId;
    }

    public BaseMessage(String id, Map<String, Object> tags) {
        this(id);
        if (tags != null) {
            this.tags.putAll(tags);
        }
    }

    public BaseMessage(String id, String replyToId, Map<String, Object> tags) {
        this(id, replyToId);
        if (tags != null) {
            this.tags.putAll(tags);
        }
    }

    public String getId() {
        return this.id;
    }

    public BaseMessage setReplyToId(String replyToId) {
        this.replyToId = replyToId;
        return this;
    }

    public String getReplyToId() {
        return this.replyToId;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * Get tick's timestamp as string.
     * 
     * @param format
     * @return
     */
    public String getTimestampStr(String format) {
        return DateFormatUtils.toString(getTimestamp(), format);
    }

    public BaseMessage addTag(String name, Object value) {
        tags.put(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("id", id).append("reply-to", replyToId)
                .append("timestamp", getTimestampStr(DateFormatUtils.DF_ISO8601))
                .append("tags", tags);
        return tsb.toString();
    }

    /**
     * Serialize this message to bytes.
     * 
     * @return
     */
    public byte[] toBytes() {
        return SerializationUtils.toByteArray(this);
    }

    /**
     * Deserialize a message from bytes.
     * 
     * @param data
     * @param clazz
     * @return
     */
    public static <T extends BaseMessage> T fromBytes(byte[] data, Class<T> clazz) {
        return SerializationUtils.fromByteArray(data, clazz);
    }

}
