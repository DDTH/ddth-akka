package com.github.ddth.akka.qnd.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class BaseQnd {

    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    private static Logger LOGGER = LoggerFactory
            .getLogger(QndClusterTickFanOutGlobalSingleton2.class);

    protected static ActorSystem startActorSystem(String configFile, Class<?>... actors) {
        Config config = TypesafeConfigUtils.loadConfig(AkkaUtils.class.getClassLoader(), configFile,
                true);
        String actorSystemName = TypesafeConfigUtils.getStringOptional(config, "akka_system.name")
                .orElse("my-actor-system");

        ActorSystem actorSystem = AkkaUtils.createActorSystem(actorSystemName, config);
        LOGGER.info("Created actor system: " + actorSystem);
        System.out.println(actorSystem);
        for (Class<?> cl : actors) {
            ActorRef actorRef = actorSystem.actorOf(Props.create(cl), cl.getSimpleName());
            LOGGER.info("Created actor [" + actorRef.path() + "].");
        }
        return actorSystem;
    }
}
