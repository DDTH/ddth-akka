package com.github.ddth.akka.qnd.cluster;

import akka.actor.ActorSystem;
import com.github.ddth.akka.cluster.MasterActor;
import com.github.ddth.akka.cluster.scheduling.ClusterTickFanOutActor;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.WorkerCoordinationPolicy;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.typesafe.config.Config;

import java.io.File;
import java.util.Date;

public class QndClusterTickFanOutNonClusterConfig extends BaseQnd {
    @Scheduling(value = "*/5 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON)
    private static class MyWorker1 extends BaseWorker {
        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path() + "}: " + tick.getId() + " / " + DateFormatUtils
                    .toString(now, DateFormatUtils.DF_ISO8601) + " / " + DateFormatUtils
                    .toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601) + " / " + (now.getTime() - tick
                    .getTimestamp().getTime()));
        }
    }

    @Scheduling(value = "*/7 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON)
    private static class MyWorker2 extends BaseWorker {
        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path() + "}: " + tick.getId() + " / " + DateFormatUtils
                    .toString(now, DateFormatUtils.DF_ISO8601) + " / " + DateFormatUtils
                    .toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601) + " / " + (now.getTime() - tick
                    .getTimestamp().getTime()));
        }
    }

    public static void main(String[] args) throws Exception {
        File configFile = new File("ddth-akka-core/src/main/java/com/github/ddth/akka/default-akka-standalone.conf");
        Config config = TypesafeConfigUtils.loadConfig(configFile, true);
        ActorSystem actorSystem = startActorSystem(config, MasterActor.class, MyWorker1.class, MyWorker2.class,
                ClusterTickFanOutActor.class);
        Thread.sleep(30000);
        actorSystem.terminate();
    }
}
