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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class QndClusterTickFanOutLocalSingleton2 extends BaseQnd {
    private static Logger LOGGER = LoggerFactory.getLogger(QndClusterTickFanOutLocalSingleton2.class);
    private static Random RAND = new Random(System.currentTimeMillis());

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.LOCAL_SINGLETON)
    static class MyWorker extends BaseClusterWorker {
        protected void logBusy(TickMessage tick, boolean isGlobal) {
            if (isGlobal) {
                LOGGER.warn("\t{" + getCluster().selfMember().address() + ":" + getActorPath().name()
                        + "} Received TICK message, but another instance is taking the task. " + tick);
            } else {
                LOGGER.warn("\t{" + getCluster().selfMember().address() + ":" + getActorPath().name()
                        + "} Received TICK message, but I am busy! " + tick);
            }
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            LOGGER.info("{" + getCluster().selfMember().address() + ":" + self().path().name() + "}: " + tick.getId()
                    + " / " + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / " + DateFormatUtils
                    .toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601) + " / " + (now.getTime() - tick
                    .getTimestamp().getTime()));
            long sleepTime = 2700 + RAND.nextInt(1000);
            LOGGER.info("\t{" + getCluster().selfMember().address() + ":" + getActorPath().name() + "} sleeping for "
                    + sleepTime);
            Thread.sleep(sleepTime);
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
