package com.github.ddth.akka.qnd;

import java.util.Date;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.CronFormat;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.akka.scheduling.tickfanout.SingleNodeTickFanOutActor;
import com.github.ddth.commons.utils.DateFormatUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class QndSingleNodeTickFanOut {

    @Scheduling("*/5 * *")
    private static class MyWorker1 extends BaseWorker {
        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    private static class MyWorker2 extends BaseWorker {
        @Override
        protected CronFormat getScheduling() {
            return CronFormat.parse("*/7 * *");
        }

        @Override
        protected void doJob(String lockId, TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem = AkkaUtils.createActorSystem("my-actor-system");
        try {
            System.out.println(actorSystem);

            actorSystem.actorOf(Props.create(MyWorker1.class), "worker1");
            actorSystem.actorOf(Props.create(MyWorker2.class), "worker2");

            ActorRef tickFanOut = SingleNodeTickFanOutActor.newInstance(actorSystem);
            System.out.println(tickFanOut);
            Thread.sleep(60000);
            actorSystem.stop(tickFanOut);
        } finally {
            actorSystem.terminate();
        }
    }

}
