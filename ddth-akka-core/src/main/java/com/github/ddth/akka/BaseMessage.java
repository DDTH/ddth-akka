package com.github.ddth.akka;

import com.github.ddth.akka.utils.AkkaUtils;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.commons.utils.MapUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Alias of {@link #setTag(String, Object).}
     *
     * @param name
     * @param value
     * @return
     */
    public BaseMessage addTag(String name, Object value) {
        return setTag(name, value);
    }

    /**
     * Attach a tag & value.
     *
     * @param name
     * @param value
     * @return
     * @since 1.0.1
     */
    public BaseMessage setTag(String name, Object value) {
        tags.put(name, value);
        return this;
    }

    /**
     * Get an attached tag value.
     *
     * @param name
     * @return
     * @since 1.0.1
     */
    public Object getTag(String name) {
        return tags.get(name);
    }

    /**
     * Get an attached tag value.
     *
     * @param name
     * @return
     * @since 1.0.1
     */
    public Optional<Object> getTagOptional(String name) {
        return Optional.ofNullable(getTag(name));
    }

    /**
     * Get an attached tag value.
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     * @since 1.0.1
     */
    public <T> T getTag(String name, Class<T> clazz) {
        return MapUtils.getValue(tags, name, clazz);
    }

    /**
     * Get an attached tag value.
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     * @since 1.0.1
     */
    public <T> Optional<T> getTagOptional(String name, Class<T> clazz) {
        return Optional.ofNullable(getTag(name, clazz));
    }

    /**
     * Get an attached tag value as date. If the attached value is a string, parse it as a {@link Date} using the specified date-time format.
     *
     * @param name
     * @param df   datetime format string
     * @return
     * @since 1.0.1
     */
    public Date getTagAsDate(String name, String df) {
        return MapUtils.getDate(tags, name, df);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("id", id).append("reply-to", replyToId)
                .append("timestamp", getTimestampStr(DateFormatUtils.DF_ISO8601)).append("tags", tags);
        return tsb.toString();
    }

    /**
     * Serialize this message to bytes.
     *
     * @return
     */
    public byte[] toBytes() {
        return SerializationUtils.toByteArrayFst(this);
    }

    /**
     * Deserialize a message from bytes.
     *
     * @param data
     * @param clazz
     * @return
     */
    public static <T extends BaseMessage> T fromBytes(byte[] data, Class<T> clazz) {
        return SerializationUtils.fromByteArrayFst(data, clazz);
    }
}
