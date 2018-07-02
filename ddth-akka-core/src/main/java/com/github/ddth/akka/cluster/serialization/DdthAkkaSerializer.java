package com.github.ddth.akka.cluster.serialization;

import com.github.ddth.commons.utils.SerializationUtils;

import akka.serialization.JSerializer;

/**
 * Serializer to serialize "tick"-messages.
 * 
 * <p>
 * This can be used as an out-of-the-box serializer for actor system, too.
 * </p>
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.3
 */
public class DdthAkkaSerializer extends JSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public int identifier() {
        return 19_810_301;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean includeManifest() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] toBinary(Object o) {
        return SerializationUtils.toByteArray(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromBinaryJava(byte[] bytes, Class<?> manifest) {
        return SerializationUtils.fromByteArray(bytes, manifest);
    }
}
