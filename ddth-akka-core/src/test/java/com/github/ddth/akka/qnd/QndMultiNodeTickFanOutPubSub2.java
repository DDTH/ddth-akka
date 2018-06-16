package com.github.ddth.akka.qnd;

import java.util.Date;
import java.util.Random;

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

public class QndMultiNodeTickFanOutPubSub2 {

    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    static Logger LOGGER = LoggerFactory.getLogger(QndMultiNodeTickFanOutPubSub2.class);
    static Random RAND = new Random(System.currentTimeMillis());

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON)
    static class MyWorker1 extends BaseWorker {
        public MyWorker1(IDLock dlock) {
            super(dlock, 5000);
        }

        protected void logBusy(TickMessage tick, boolean isGlobal) {
            // EMPTY
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            LOGGER.info("{" + self().path() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
            long sleepTime = 2300 + RAND.nextInt(1000);
            LOGGER.info("\t{" + self().path() + "} sleepping for " + sleepTime);
            Thread.sleep(sleepTime);
            unlock(lockId);
        }
    }

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON)
    static class MyWorker2 extends BaseWorker {
        public MyWorker2(IDLock dlock) {
            super(dlock, 5000);
        }

        protected void logBusy(TickMessage tick, boolean isGlobal) {
            // EMPTY
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            LOGGER.info("{" + self().path() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
            long sleepTime = 2500 + RAND.nextInt(1000);
            LOGGER.info("\t{" + self().path() + "} sleepping for " + sleepTime);
            Thread.sleep(sleepTime);
            unlock(lockId);
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
                    System.out.println("Actor: " + actorSystem
                            .actorOf(Props.create(MyWorker1.class, dlock1), "worker1"));

                    IDLock dlock2 = dlockFactory.createLock("worker");
                    System.out.println("Actor: " + actorSystem
                            .actorOf(Props.create(MyWorker2.class, dlock2), "worker2"));

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
