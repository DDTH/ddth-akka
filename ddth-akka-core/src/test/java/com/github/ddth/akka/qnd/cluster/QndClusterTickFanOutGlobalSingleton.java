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

public class QndClusterTickFanOutGlobalSingleton extends BaseQnd {
    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    private final static String DF = "HH:mm:ss.SSS";

    private static Logger LOGGER = LoggerFactory.getLogger(QndClusterTickFanOutGlobalSingleton.class);
    private static Random RAND = new Random(System.currentTimeMillis());

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON)
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
            if (isGlobal) {
                LOGGER.warn("\t{" + getActorPath().name()
                        + "} Received TICK message, but another instance is taking the task. " + tick);
            } else {
                LOGGER.warn("\t{" + getActorPath().name() + "} Received TICK message, but I am busy! " + tick);
            }
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            try {
                System.out.println(
                        "{" + self().path().name() + "}: Tick {" + tick.getId() + "} from {" + sender().path().name()
                                + " : " + tick.getTag(TickFanOutActor.TAG_SENDDER_ADDR) + "} / Now " + DateFormatUtils
                                .toString(now, DF) + " / TickTime " + DateFormatUtils.toString(tick.getTimestamp(), DF)
                                + " / Lag " + (now.getTime() - tick.getTimestamp().getTime()));
                long sleepTime = 2300 + RAND.nextInt(1000);
                LOGGER.info("\t{" + getActorPath().name() + "} sleeping for " + sleepTime);
                Thread.sleep(sleepTime);
            } finally {
                if (!StringUtils.isBlank(lockId) && System.currentTimeMillis() - now.getTime() > 1000) {
                    ddUnlock(getLockKey(), lockId);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        File configFile = new File(
                "ddth-akka-core/src/test/java/com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf");
        Config config = TypesafeConfigUtils.loadConfig(configFile, true);
        ActorSystem actorSystem = startActorSystem(config, MasterActor.class, MyWorker.class,
                ClusterTickFanOutActor.class);
        Thread.sleep(30000);
        actorSystem.terminate();
    }
}
