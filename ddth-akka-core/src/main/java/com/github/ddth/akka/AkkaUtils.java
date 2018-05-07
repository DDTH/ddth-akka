package com.github.ddth.akka;

import com.github.ddth.commons.utils.IdGenerator;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.typesafe.config.Config;

import akka.actor.ActorSystem;

/**
 * Internal utility class.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class AkkaUtils {

    /**
     * Default name of dispatcher used for executing workers' tasks.
     */
    public final static String AKKA_DISPATCHER_WORKERS = "akka.actor.worker-dispatcher";

    private final static IdGenerator idGen = IdGenerator.getInstance(IdGenerator.getMacAddr());

    /**
     * Generate a unique id string.
     * 
     * @return
     */
    public static String nextId() {
        return idGen.generateId128Ascii().toLowerCase();
    }

    /**
     * Create an {@link ActorSystem} with default configurations.
     * 
     * @param name
     * @return
     */
    public static ActorSystem createActorSystem(String name) {
        Config config = TypesafeConfigUtils.loadConfig(AkkaUtils.class.getClassLoader(),
                "com/github/ddth/akka/default-akka-standalone.conf", true);
        return ActorSystem.create(name, config);
    }

    /**
     * Create an {@link ActorSystem} with specified configurations.
     * 
     * @param name
     * @param config
     * @return
     */
    public static ActorSystem createActorSystem(String name, Config config) {
        return ActorSystem.create(name, config);
    }

    /**
     * Shorten class name, i.e. {@code com.github.ddth.akka} becomes
     * {@code c.g.d.a.AkkaUtils}.
     * 
     * @param clazz
     * @return
     */
    public static String shortenClassName(Class<?> clazz) {
        String clazzName = clazz.getName();
        String[] tokens = clazzName.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            sb.append(tokens[i].charAt(0)).append(".");
        }
        sb.append(tokens[tokens.length - 1]);
        return sb.toString();
    }

}
