package com.github.ddth.akka.qnd;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.CronFormat;
import com.github.ddth.akka.scheduling.TickFanOutActor;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.akka.scheduling.tickfanout.SingleNodeTickFanOutActor;
import com.github.ddth.commons.utils.DateFormatUtils;

import java.util.Date;

public class QndSingleNodeTickFanOut {
    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    private final static String DF = "HH:mm:ss.SSS";

    @Scheduling("*/5 * *")
    private static class MyWorker1 extends BaseWorker {
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

    private static class MyWorker2 extends BaseWorker {
        public MyWorker2() {
            setHandleMessageAsync(false);
        }

        @Override
        protected CronFormat getScheduling() {
            return CronFormat.parse("*/7 * *");
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
        ActorSystem actorSystem = AkkaUtils.createActorSystem("my-actor-system");
        try {
            System.out.println("ActorSystem: " + actorSystem);

            actorSystem.actorOf(Props.create(MyWorker1.class), "worker1");
            actorSystem.actorOf(Props.create(MyWorker2.class), "worker2");

            ActorRef tickFanOut = SingleNodeTickFanOutActor.newInstance(actorSystem);
            System.out.println("Tick fan-out: " + tickFanOut);
            Thread.sleep(60000);
            actorSystem.stop(tickFanOut);
        } finally {
            actorSystem.terminate();
        }
    }
}
