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

public class QndClusterTickFanOutGlobalSingleton3 extends BaseQnd {

    private static Logger LOGGER = LoggerFactory
            .getLogger(QndClusterTickFanOutGlobalSingleton3.class);
    private static Random RAND = new Random(System.currentTimeMillis());

    static abstract class MyWorker extends BaseClusterWorker {
        @Override
        protected void logBusy(TickMessage tick, boolean isGlobal) {
            if (isGlobal) {
                String id = getCluster().selfMember().address() + ":" + self().path().name();
                LOGGER.warn("\t{" + id
                        + "} Received TICK message, but another instance is taking the task. "
                        + tick.getTimestampStr("HH:mm:ss") + " / " + sender().path());
            } else {
                LOGGER.warn(
                        "\t{" + getActorPath().name() + "} Received TICK message, but I am busy! "
                                + tick.getTimestampStr("HH:mm:ss") + " / " + sender().path());
            }
        }

        protected abstract long getSleepTime();

        protected abstract AtomicLong lastTimestamp();

        protected abstract AtomicLong lastSleep();

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            long lastT = lastTimestamp().getAndSet(now.getTime());
            long lastS = lastSleep().get();
            long t = tick.getTimestamp().getTime();
            try {
                String id = getCluster().selfMember().address() + ":" + self().path().name();
                LOGGER.info("{" + id + "}: " + tick.getId() + " / "
                        + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                        + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                        + " / " + (now.getTime() - tick.getTimestamp().getTime()) + " / "
                        + (lastT + lastS < t));
                long sleepTime = getSleepTime() + RAND.nextInt(1000);
                LOGGER.info("\t{" + getActorPath().name() + "} sleepping for " + sleepTime);
                lastSleep().set(sleepTime);
                Thread.sleep(sleepTime);
            } finally {
                if (!StringUtils.isBlank(lockId)
                        && System.currentTimeMillis() - now.getTime() > 1000) {
                    ddUnlock(getLockKey(), lockId);
                }
            }
        }
    }

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON, lockTime = 10000)
    static class MyWorker1 extends MyWorker {
        private static AtomicLong lastTimestamp = new AtomicLong();
        private static AtomicLong lastSleep = new AtomicLong();

        protected AtomicLong lastTimestamp() {
            return lastTimestamp;
        }

        protected AtomicLong lastSleep() {
            return lastSleep;
        }

        @Override
        protected String getGroupId() {
            return "group-id-1";
        }

        @Override
        protected String getLockKey() {
            return "lock-key-1";
        }

        @Override
        protected String getDdKeyId() {
            return "ddkey-id-1";
        }

        protected long getSleepTime() {
            return 2500;
        }
    }

    @Scheduling(value = "*/5 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON, lockTime = 10000)
    static class MyWorker2 extends MyWorker {
        private static AtomicLong lastTimestamp = new AtomicLong();
        private static AtomicLong lastSleep = new AtomicLong();

        protected AtomicLong lastTimestamp() {
            return lastTimestamp;
        }

        protected AtomicLong lastSleep() {
            return lastSleep;
        }

        @Override
        protected String getGroupId() {
            return "group-id-2";
        }

        @Override
        protected String getLockKey() {
            return "lock-key-2";
        }

        @Override
        protected String getDdKeyId() {
            return "ddkey-id-2";
        }

        protected long getSleepTime() {
            return 4500;
        }
    }

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem1 = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf", MasterActor.class,
                MyWorker1.class, MyWorker2.class, ClusterTickFanOutActor.class);
        ActorSystem actorSystem2 = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node2.conf", MasterActor.class,
                MyWorker1.class, MyWorker2.class, ClusterTickFanOutActor.class);
        Thread.sleep(60000);
        actorSystem1.terminate();
        actorSystem2.terminate();
    }
}
