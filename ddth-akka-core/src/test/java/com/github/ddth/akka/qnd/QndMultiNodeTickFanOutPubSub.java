package com.github.ddth.akka.qnd;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.TickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.akka.scheduling.tickfanout.MultiNodePubSubBasedTickFanOutActor;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.impl.redis.RedisDLockFactory;
import com.github.ddth.pubsub.impl.universal.idint.UniversalRedisPubSubHub;

import java.util.Date;

public class QndMultiNodeTickFanOutPubSub {
    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    private final static String DF = "HH:mm:ss.SSS";

    @Scheduling("*/3 * *")
    private static class MyWorker1 extends BaseWorker {
        public MyWorker1() {
            setHandleMessageAsync(true);
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) {
            Date now = new Date();
            System.out.println(
                    "{" + self().path().name() + "}: Tick {" + tick.getId() + "} from {" + sender().path().name()
                            + " : " + tick.getTag(TickFanOutActor.TAG_SENDDER_ADDR) + "} / Now " + DateFormatUtils
                            .toString(now, DF) + " / TickTime " + DateFormatUtils.toString(tick.getTimestamp(), DF)
                            + " / Lag " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    @Scheduling("*/5 * *")
    private static class MyWorker2 extends BaseWorker {
        public MyWorker2() {
            setHandleMessageAsync(false);
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) {
            Date now = new Date();
            System.out.println(
                    "{" + self().path().name() + "}: Tick {" + tick.getId() + "} from {" + sender().path().name()
                            + " : " + tick.getTag(TickFanOutActor.TAG_SENDDER_ADDR) + "} / Now " + DateFormatUtils
                            .toString(now, DF) + " / TickTime " + DateFormatUtils.toString(tick.getTimestamp(), DF)
                            + " / Lag " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    public static void main(String[] args) throws Exception {
        String redisHostsAndPorts = "localhost:6379";
        String redisPassword = null;
        try (RedisDLockFactory dlockFactory = new RedisDLockFactory()) {
            dlockFactory.setRedisHostAndPort(redisHostsAndPorts).setRedisPassword(redisPassword)
                    .setLockNamePrefix("dlock-").init();

            try (UniversalRedisPubSubHub pubSub = new UniversalRedisPubSubHub()) {
                pubSub.setRedisHostAndPort(redisHostsAndPorts).setRedisPassword(redisPassword).init();

                ActorSystem actorSystem = AkkaUtils.createActorSystem("my-actor-system");
                try {
                    System.out.println("Actor system: " + actorSystem);

                    System.out.println("Worker1: " + actorSystem.actorOf(Props.create(MyWorker1.class), "worker1"));
                    System.out.println("Worker2: " + actorSystem.actorOf(Props.create(MyWorker2.class), "worker2"));

                    IDLock dlock = dlockFactory.createLock("demo");
                    System.out.println("DLock: " + dlock);

                    ActorRef tickFanOut = MultiNodePubSubBasedTickFanOutActor
                            .newInstance(actorSystem, dlock, pubSub, "pubSubChannel");
                    System.out.println("Tick fan-out: " + tickFanOut);

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
