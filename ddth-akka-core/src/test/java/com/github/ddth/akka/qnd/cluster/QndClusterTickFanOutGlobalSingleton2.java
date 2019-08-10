package com.github.ddth.akka.qnd.cluster;

import akka.actor.ActorSystem;
import com.github.ddth.akka.cluster.MasterActor;
import com.github.ddth.akka.cluster.scheduling.BaseClusterWorker;
import com.github.ddth.akka.cluster.scheduling.ClusterTickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.WorkerCoordinationPolicy;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class QndClusterTickFanOutGlobalSingleton2 extends BaseQnd {
    private static Logger LOGGER = LoggerFactory.getLogger(QndClusterTickFanOutGlobalSingleton2.class);
    private static Random RAND = new Random(System.currentTimeMillis());

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON, lockTime = 10000)
    static class MyWorker extends BaseClusterWorker {
        private static AtomicLong lastTimestamp = new AtomicLong();
        private static AtomicLong lastSleep = new AtomicLong();

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
            if (isGlobal) {
                String id = getCluster().selfMember().address() + ":" + self().path().name();
                LOGGER.warn("\t{" + id + "} Received TICK message, but another instance is taking the task. " + tick
                        .getTimestampStr("HH:mm:ss") + " / " + sender().path());
            } else {
                LOGGER.warn("\t{" + getActorPath().name() + "} Received TICK message, but I am busy! " + tick
                        .getTimestampStr("HH:mm:ss") + " / " + sender().path());
            }
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            final String DF = "HH:mm:ss.SSS";
            Date now = new Date();
            long lastT = lastTimestamp.getAndSet(now.getTime());
            long lastS = lastSleep.get();
            long t = tick.getTimestamp().getTime();
            try {
                String id = getCluster().selfMember().address() + ":" + self().path().name();
                LOGGER.info("{" + id + "}: " + tick.getId() + " / " + DateFormatUtils.toString(now, DF) + " / "
                        + DateFormatUtils.toString(tick.getTimestamp(), DF) + " / " + (now.getTime() - tick
                        .getTimestamp().getTime()) + " / " + (lastT + lastS < t));
                long sleepTime = 2300 + RAND.nextInt(1000);
                LOGGER.info("\t{" + getActorPath().name() + "} sleeping for " + sleepTime);
                lastSleep.set(sleepTime);
                Thread.sleep(sleepTime);
            } finally {
                if (!StringUtils.isBlank(lockId) && System.currentTimeMillis() - now.getTime() > 1000) {
                    ddUnlock(getLockKey(), lockId);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem1, actorSystem2;

        System.err.println("Starting actor system 1...");
        {
            File configFile1 = new File(
                    "ddth-akka-core/src/test/java/com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf");
            Config config1 = TypesafeConfigUtils.loadConfig(configFile1, true);
            actorSystem1 = startActorSystem(config1, MasterActor.class, MyWorker.class, ClusterTickFanOutActor.class);
        }

        System.err.println("Starting actor system 2...");
        {
            File configFile2 = new File(
                    "ddth-akka-core/src/test/java/com/github/ddth/akka/qnd/cluster/akka-cluster-node2.conf");
            Config config2 = TypesafeConfigUtils.loadConfig(configFile2, true);
            actorSystem2 = startActorSystem(config2, MasterActor.class, MyWorker.class, ClusterTickFanOutActor.class);
        }

        Thread.sleep(60000);

        actorSystem1.terminate();
        actorSystem2.terminate();
    }
}
