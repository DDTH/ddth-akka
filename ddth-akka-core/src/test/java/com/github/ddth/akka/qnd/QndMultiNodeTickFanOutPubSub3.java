package com.github.ddth.akka.qnd;

import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.WorkerCoordinationPolicy;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.akka.scheduling.tickfanout.MultiNodePubSubBasedTickFanOutActor;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.impl.redis.RedisDLockFactory;
import com.github.ddth.pubsub.impl.universal.idint.UniversalRedisPubSubHub;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class QndMultiNodeTickFanOutPubSub3 {

    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    static Logger LOGGER = LoggerFactory.getLogger("QndMultiNodeTickFanOutPubSub2");
    static Random RAND = new Random(System.currentTimeMillis());

    @Scheduling(value = "*/3 * *", getWorkerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON)
    public static class GlobalSingletonWorker extends BaseWorker {

        public GlobalSingletonWorker(IDLock dlock, Long dlockTimeMs) {
            super(dlock, dlockTimeMs);
        }

        @Override
        protected void doJob(String dlockId, TickMessage tick) throws InterruptedException {
            Date now = new Date();
            try {
                LOGGER.info("{" + self().path() + "}: " + tick.getId() + " / "
                        + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                        + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                        + " / " + (now.getTime() - tick.getTimestamp().getTime()));

                int sleepMs = 2400 + RAND.nextInt(1000);
                Thread.sleep(sleepMs);
            } finally {
                if (!StringUtils.isBlank(dlockId)
                        && System.currentTimeMillis() - now.getTime() > 1000) {
                    unlock(dlockId);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String redisHostsAndPorts = "localhost:6379";
        String redisPassword = null;
        try (RedisDLockFactory dlockFactory = new RedisDLockFactory()) {
            dlockFactory.setRedisHostAndPort(redisHostsAndPorts).setRedisPassword(redisPassword)
                    .setLockNamePrefix("dlock-").init();

            try (UniversalRedisPubSubHub pubSub = new UniversalRedisPubSubHub()) {
                pubSub.setRedisHostAndPort(redisHostsAndPorts).setRedisPassword(redisPassword)
                        .init();

                ActorSystem actorSystem = AkkaUtils.createActorSystem("my-actor-system");
                try {
                    System.out.println("Actor system: " + actorSystem);

                    IDLock dlock1 = dlockFactory.createLock("worker");
                    System.out.println("Actor: " + actorSystem.actorOf(
                            Props.create(GlobalSingletonWorker.class, dlock1, 5000L), "worker1"));

                    IDLock dlock2 = dlockFactory.createLock("worker");
                    System.out.println("Actor: " + actorSystem.actorOf(
                            Props.create(GlobalSingletonWorker.class, dlock2, 5000L), "worker2"));

                    IDLock dlock = dlockFactory.createLock("demo");
                    System.out.println("DLock: " + dlock);

                    ActorRef tickFanOut = MultiNodePubSubBasedTickFanOutActor
                            .newInstance(actorSystem, dlock, pubSub, "pubSubChannel");
                    System.out.println(tickFanOut);

                    Thread.sleep(60000);

                    actorSystem.stop(tickFanOut);

                    Thread.sleep(1000);
                } finally {
                    actorSystem.terminate().value();
                }
            }
        }
    }
}
