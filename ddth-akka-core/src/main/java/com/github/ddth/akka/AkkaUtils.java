package com.github.ddth.akka;

import akka.actor.ActorSystem;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.typesafe.config.Config;

/**
 * Akka utility class.
 *
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class AkkaUtils {
    /**
     * Create an {@link ActorSystem} with default configurations.
     *
     * <p>Default configurations are loaded from {@code com/github/ddth/akka/default-akka-standalone.conf}</p>
     *
     * @param name
     * @return
     */
    public static ActorSystem createActorSystem(String name) {
        Config config = TypesafeConfigUtils
                .loadConfig(AkkaUtils.class.getClassLoader(), "com/github/ddth/akka/default-akka-standalone.conf",
                        true);
        return ActorSystem.create(name, config);
    }

    /**
     * Create an {@link ActorSystem} with specific configurations.
     *
     * @param name
     * @param config
     * @return
     */
    public static ActorSystem createActorSystem(String name, Config config) {
        return ActorSystem.create(name, config);
    }
}
