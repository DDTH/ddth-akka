package com.github.ddth.akka.scheduling;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.commons.utils.SerializationUtils;

/**
 * A message that encapsulates a "tick".
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class TickMessage implements Serializable {

    private static final long serialVersionUID = "0.1.0".hashCode();

    /**
     * "Tick"'s unique id.
     */
    public final String id;

    /**
     * "Tick's" timestamp when the tick is fired.
     */
    public final Date timestamp;

    public final Map<String, Object> tags = new HashMap<>();

    public TickMessage() {
        id = AkkaUtils.nextId();
        timestamp = new Date();
    }

    public TickMessage(Map<String, Object> tags) {
        this();
        if (tags != null) {
            this.tags.putAll(tags);
        }
    }

    public TickMessage(String id) {
        this.id = id;
        timestamp = new Date();
    }

    public TickMessage(String id, Map<String, Object> tags) {
        this(id);
        if (tags != null) {
            this.tags.putAll(tags);
        }
    }

    public String getId() {
        return this.id;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    /**
     * Get tick's timestamp as string.
     * 
     * @param format
     * @return
     * @since 0.1.2
     */
    public String getTimestampStr(String format) {
        return DateFormatUtils.toString(getTimestamp(), format);
    }

    public TickMessage addTag(String name, Object value) {
        tags.put(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("id", id).append("timestamp", getTimestampStr(DateFormatUtils.DF_ISO8601))
                .append("tags", tags);
        return tsb.toString();
    }

    /**
     * Serialize this tick message to bytes.
     * 
     * @return
     */
    public byte[] toBytes() {
        return SerializationUtils.toByteArray(this);
    }

    /**
     * Deserialize a tick message from bytes.
     * 
     * @param data
     * @param clazz
     * @return
     */
    public static <T extends TickMessage> T fromBytes(byte[] data, Class<T> clazz) {
        return SerializationUtils.fromByteArray(data, clazz);
    }

}
