package com.github.ddth.akka.qnd.cluster;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ddth.akka.cluster.MasterActor;
import com.github.ddth.akka.cluster.scheduling.BaseClusterWorker;
import com.github.ddth.akka.cluster.scheduling.ClusterTickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.WorkerCoordinationPolicy;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.commons.utils.DateFormatUtils;

import akka.actor.ActorSystem;

public class QndClusterTickFanOutGlobalSingleton2 extends BaseQnd {

    private static Logger LOGGER = LoggerFactory
            .getLogger(QndClusterTickFanOutGlobalSingleton2.class);
    private static Random RAND = new Random(System.currentTimeMillis());
    private static AtomicLong lastTimestamp = new AtomicLong();
    private static AtomicLong lastSleep = new AtomicLong();

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON, lockTime = 10000)
    static class MyWorker extends BaseClusterWorker {
        @Override
        protected String getGroupId() {
            return "group-id";
        }

        @Override
        protected String getLockKey() {
            return "lock-key";
        }

        @Override
        protected String getDdKeyId() {
            return "ddkey-id";
        }

        @Override
        protected void logBusy(TickMessage tick, boolean isGlobal) {
            // if (isGlobal) {
            // LOGGER.warn("\t{" + getActorPath().name()
            // + "} Received TICK message, but another instance is taking the
            // task. "
            // + tick);
            // } else {
            // LOGGER.warn("\t{" + getActorPath().name()
            // + "} Received TICK message, but I am busy! " + tick);
            // }
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            long lastT = lastTimestamp.getAndSet(now.getTime());
            long lastS = lastSleep.get();
            long t = tick.getTimestamp().getTime();
            try {
                LOGGER.info("{" + self().path().name() + "}: " + tick.getId() + " / "
                        + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                        + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                        + " / " + (now.getTime() - tick.getTimestamp().getTime()) + " / "
                        + (lastT + lastS < t));
                long sleepTime = 2500 + RAND.nextInt(1000);
                LOGGER.info("\t{" + getActorPath().name() + "} sleepping for " + sleepTime);
                lastSleep.set(sleepTime);
                Thread.sleep(sleepTime);
            } finally {
                if (!StringUtils.isBlank(lockId)
                        && System.currentTimeMillis() - now.getTime() > 1000) {
                    ddUnlock(getLockKey(), lockId);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem1 = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf", MasterActor.class,
                MyWorker.class, ClusterTickFanOutActor.class);
        ActorSystem actorSystem2 = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node2.conf", MasterActor.class,
                MyWorker.class, ClusterTickFanOutActor.class);
        Thread.sleep(60000);
        actorSystem1.terminate();
        actorSystem2.terminate();
    }
}