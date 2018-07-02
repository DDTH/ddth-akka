package com.github.ddth.akka.qnd.cluster;

import java.util.Date;
import java.util.Random;

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

public class QndClusterTickFanOutLocalSingleton2 extends BaseQnd {

    private static Logger LOGGER = LoggerFactory
            .getLogger(QndClusterTickFanOutLocalSingleton2.class);
    private static Random RAND = new Random(System.currentTimeMillis());

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.LOCAL_SINGLETON)
    static class MyWorker extends BaseClusterWorker {
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
            LOGGER.info("{" + self().path().name() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
            long sleepTime = 2700 + RAND.nextInt(1000);
            LOGGER.info("\t{" + getActorPath().name() + "} sleepping for " + sleepTime);
            Thread.sleep(sleepTime);
        }
    }

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem1 = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf", MasterActor.class,
                MyWorker.class, ClusterTickFanOutActor.class);
        ActorSystem actorSystem2 = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node2.conf", MasterActor.class,
                MyWorker.class, ClusterTickFanOutActor.class);
        Thread.sleep(30000);
        actorSystem1.terminate();
        actorSystem2.terminate();
    }
}
