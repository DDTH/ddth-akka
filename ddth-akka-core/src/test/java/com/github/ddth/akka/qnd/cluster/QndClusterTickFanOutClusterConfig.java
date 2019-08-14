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

import java.io.File;
import java.util.Date;

public class QndClusterTickFanOutClusterConfig extends BaseQnd {
    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    private final static String DF = "HH:mm:ss.SSS";

    @Scheduling(value = "*/5 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.TAKE_ALL_TASKS)
    private static class MyWorker1 extends BaseClusterWorker {
        public MyWorker1() {
            setHandleMessageAsync(true);
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) {
            Date now = new Date();
            System.out.println(
                    "{" + self().path().name() + "}: Tick {" + tick.getId() + "} from {" + sender().path().name()
                            + " : " + tick.getTag(TickFanOutActor.TAG_SENDDER_ADDR) + "} / Now " + DateFormatUtils
                            .toString(now, DF) + " / TickTime " + DateFormatUtils.toString(tick.getTimestamp(), DF)
                            + " / Lag " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    @Scheduling(value = "*/7 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.TAKE_ALL_TASKS)
    private static class MyWorker2 extends BaseClusterWorker {
        public MyWorker2() {
            setHandleMessageAsync(false);
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) {
            Date now = new Date();
            System.out.println(
                    "{" + self().path().name() + "}: Tick {" + tick.getId() + "} from {" + sender().path().name()
                            + " : " + tick.getTag(TickFanOutActor.TAG_SENDDER_ADDR) + "} / Now " + DateFormatUtils
                            .toString(now, DF) + " / TickTime " + DateFormatUtils.toString(tick.getTimestamp(), DF)
                            + " / Lag " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    public static void main(String[] args) throws Exception {
        File configFile = new File(
                "ddth-akka-core/src/test/java/com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf");
        Config config = TypesafeConfigUtils.loadConfig(configFile, true);
        ActorSystem actorSystem = startActorSystem(config, MasterActor.class, MyWorker1.class, MyWorker2.class,
                ClusterTickFanOutActor.class);
        Thread.sleep(30000);
        actorSystem.terminate();
    }
}
