package com.github.ddth.akka.qnd.cluster;

import akka.actor.ActorSystem;
import com.github.ddth.akka.cluster.DistributedDataUtils.DDGetResult;
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

public class QndCluster1 extends BaseQnd {
    private static Logger LOGGER = LoggerFactory.getLogger(QndCluster1.class);

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON, lockTime = 10000)
    static class MyWorker1 extends BaseClusterWorker {
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
        protected String getDdKeyId() {
            return "ddkey-id";
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            try {
                // LOGGER.debug("\t{" + getActorPath().name() + "}: " +
                // getReplicator());
                String value = DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601);
                ddSet("key", value);
                LOGGER.warn("\t{" + getActorPath().name() + "} put: " + value);
            } finally {
                if (!StringUtils.isBlank(lockId) && System.currentTimeMillis() - now.getTime() > 1000) {
                    ddUnlock(getLockKey(), lockId);
                }
            }
        }
    }

    @Scheduling(value = "*/3 * *", workerCoordinationPolicy = WorkerCoordinationPolicy.GLOBAL_SINGLETON, lockTime = 10000)
    static class MyWorker2 extends BaseClusterWorker {
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
        protected String getDdKeyId() {
            return "ddkey-id";
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            try {
                DDGetResult result = ddGet("key");
                LOGGER.warn("\t{" + getActorPath().name() + "} get: " + result);
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
        ActorSystem actorSystem = startActorSystem(config, MasterActor.class, MyWorker1.class, MyWorker2.class,
                ClusterTickFanOutActor.class);
        Thread.sleep(30000);
        actorSystem.terminate();
    }
}
