package com.github.ddth.akka.qnd;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.WorkerCoordinationPolicy;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.akka.scheduling.tickfanout.MultiNodePubSubBasedTickFanOutActor;
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

        private AtomicLong COUNTER_TASK = new AtomicLong(0), COUNTER_BUSY = new AtomicLong(0);

        /**
         * {@inheritDoc}
         */
        @Override
        protected void logBusy(TickMessage tick, boolean isGlobal) {
            COUNTER_BUSY.incrementAndGet();
        }

        @Override
        protected void doJob(String dlockId, TickMessage tick) throws InterruptedException {
            Date now = new Date();
            try {
                COUNTER_TASK.incrementAndGet();
                // LOGGER.info("{" + self().path() + "}: " + tick.getId() + " /
                // "
                // + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) +
                // " / "
                // + DateFormatUtils.toString(tick.getTimestamp(),
                // DateFormatUtils.DF_ISO8601)
                // + " / " + (now.getTime() - tick.getTimestamp().getTime()));
                // int sleepMs = 1400 + RAND.nextInt(1000);
                // Thread.sleep(sleepMs);
                long numTask = COUNTER_TASK.get();
                long numBusy = COUNTER_BUSY.get();
                LOGGER.info("\t{" + self().path() + "} " + numTask + " / " + numBusy + " / "
                        + (numTask * 100.0 / (numTask + numBusy)));
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

                    for (int i = 0; i < 4; i++) {
                        IDLock dlock = dlockFactory.createLock("worker");
                        System.out.println("Actor: " + actorSystem.actorOf(
                                Props.create(GlobalSingletonWorker.class, dlock, 5000L),
                                "worker" + i));
                    }

                    IDLock dlock = dlockFactory.createLock("demo");
                    System.out.println("DLock: " + dlock);

                    ActorRef tickFanOut = MultiNodePubSubBasedTickFanOutActor
                            .newInstance(actorSystem, dlock, pubSub, "pubSubChannel");
                    System.out.println(tickFanOut);

                    Thread.sleep(600000);

                    actorSystem.stop(tickFanOut);

                    Thread.sleep(1000);
                } finally {
                    actorSystem.terminate().value();
                }
            }
        }
    }
}
