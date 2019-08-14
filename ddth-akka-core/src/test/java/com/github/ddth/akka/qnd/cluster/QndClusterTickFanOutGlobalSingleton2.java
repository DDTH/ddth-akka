package com.github.ddth.akka.qnd.cluster;

import akka.actor.ActorSystem;
import com.github.ddth.akka.cluster.MasterActor;
import com.github.ddth.akka.cluster.scheduling.BaseClusterWorker;
import com.github.ddth.akka.cluster.scheduling.ClusterTickFanOutActor;
import com.github.ddth.akka.scheduling.TickFanOutActor;
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
    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    private final static String DF = "HH:mm:ss.SSS";

    private static Logger LOGGER = LoggerFactory.getLogger(QndClusterTickFanOutGlobalSingleton2.class);
    private static Random RAND = new Random(System.currentTimeMillis());

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON, lockTime = 10000)
    static class MyWorker extends BaseClusterWorker {
        static int counter = 0;

        public MyWorker() {
            setHandleMessageAsync(counter % 2 == 0);
            counter++;
        }

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
                String myId = getCluster().selfMember().address() + "/" + self().path().name();
                LOGGER.warn("\t{" + myId + "} Received TICK message, but another instance is taking the task. " + tick
                        .getTimestampStr(DF) + " / " + sender().path());
            } else {
                LOGGER.warn("\t{" + getActorPath().name() + "} Received TICK message, but I am busy! " + tick
                        .getTimestampStr(DF) + " / " + sender().path());
            }
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            long lastT = lastTimestamp.getAndSet(now.getTime());
            long lastS = lastSleep.get();
            long t = tick.getTimestamp().getTime();
            try {
                String myId = getCluster().selfMember().address() + "/" + self().path().name();
                String senderId = sender().path().address() + "/" + sender().path().name();
                System.out.println("{" + myId + "}: Tick {" + tick.getId() + "} from {" + senderId + " : " + tick
                        .getTag(TickFanOutActor.TAG_SENDDER_ADDR) + "} / Now " + DateFormatUtils.toString(now, DF)
                        + " / TickTime " + DateFormatUtils.toString(tick.getTimestamp(), DF) + " / Lag " + (
                        now.getTime() - tick.getTimestamp().getTime()) + " / " + (lastT + lastS < t));
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
