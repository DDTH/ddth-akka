package com.github.ddth.akka.qnd;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

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
import com.google.common.util.concurrent.AtomicDoubleArray;

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

    static Logger LOGGER = LoggerFactory.getLogger(QndMultiNodeTickFanOutPubSub3.class);
    static Random RAND = new Random(System.currentTimeMillis());
    static int NUM_WORKERS = 4;
    static AtomicLongArray COUNTER_HIT = new AtomicLongArray(NUM_WORKERS);
    static AtomicLong COUNTER = new AtomicLong();
    static AtomicLong LASTTICK = new AtomicLong(0);

    @Scheduling(value = "*/5 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON)
    public static class GlobalSingletonWorker extends BaseWorker {

        private int workerId;

        public GlobalSingletonWorker(IDLock dlock, int workerId) {
            super(dlock);
            this.workerId = workerId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void logBusy(TickMessage tick, boolean isGlobal) {
            COUNTER.incrementAndGet();
        }

        @Override
        protected void doJob(String dlockId, TickMessage tick) throws InterruptedException {
            Date now = new Date();
            try {
                long delta = tick.getTimestamp().getTime() - LASTTICK.get();
                boolean valid = LASTTICK.get() == 0 || (4500 <= delta && delta <= 5500);

                long numTotal = COUNTER.incrementAndGet();
                COUNTER_HIT.incrementAndGet(workerId);
                AtomicDoubleArray rate = new AtomicDoubleArray(NUM_WORKERS);
                for (int i = 0; i < NUM_WORKERS; i++) {
                    double d = COUNTER_HIT.get(i) * 100.0 / numTotal;
                    rate.set(i, Math.round(d * 10.0) / 10.0);
                }

                LOGGER.info(self().path().name() + ": " + tick.getTimestampStr("HH:mm:ss")
                        + (valid ? " [Y]" : " [N]") + " / Delay: "
                        + (now.getTime() - tick.getTimestamp().getTime()) + "ms / Counter: "
                        + COUNTER_HIT + " / Rate: " + rate);

                LASTTICK.set(tick.getTimestamp().getTime());
                Thread.sleep(RAND.nextInt(3000));
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

                    for (int i = 0; i < NUM_WORKERS; i++) {
                        IDLock dlock = dlockFactory.createLock("worker");
                        System.out.println("Actor: " + actorSystem.actorOf(
                                Props.create(GlobalSingletonWorker.class, dlock, i), "worker" + i));
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
