package com.github.ddth.akka.qnd.cluster;

import java.util.Date;

import com.github.ddth.akka.cluster.MasterActor;
import com.github.ddth.akka.cluster.scheduling.BaseClusterWorker;
import com.github.ddth.akka.cluster.scheduling.ClusterTickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.commons.utils.DateFormatUtils;

import akka.actor.ActorSystem;

public class QndClusterTickFanOutClusterConfig extends BaseQnd {

    @Scheduling(value = "*/5 * *")
    private static class MyWorker1 extends BaseClusterWorker {
        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path().name() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    @Scheduling(value = "*/7 * *")
    private static class MyWorker2 extends BaseClusterWorker {
        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path().name() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf", MasterActor.class,
                MyWorker1.class, MyWorker2.class, ClusterTickFanOutActor.class);
        Thread.sleep(30000);
        actorSystem.terminate();
    }

}
