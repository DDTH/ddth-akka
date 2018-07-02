package com.github.ddth.akka.qnd;

import java.util.Date;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.akka.scheduling.tickfanout.MultiNodePubSubBasedTickFanOutActor;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.impl.redis.RedisDLockFactory;
import com.github.ddth.pubsub.impl.universal.idint.UniversalRedisPubSubHub;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class QndMultiNodeTickFanOutPubSub {

    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    @Scheduling("*/3 * *")
    private static class MyWorker1 extends BaseWorker {
        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    @Scheduling("*/5 * *")
    private static class MyWorker2 extends BaseWorker {
        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
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

                    System.out.println("Actor: "
                            + actorSystem.actorOf(Props.create(MyWorker1.class), "worker1"));
                    System.out.println("Actor: "
                            + actorSystem.actorOf(Props.create(MyWorker2.class), "worker2"));

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
