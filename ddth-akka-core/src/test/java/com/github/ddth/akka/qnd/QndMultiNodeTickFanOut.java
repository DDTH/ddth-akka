package com.github.ddth.akka.qnd;

import java.util.Date;

import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.scheduling.BaseWorker;
import com.github.ddth.akka.scheduling.CronFormat;
import com.github.ddth.akka.scheduling.TickMessage;
import com.github.ddth.akka.scheduling.annotation.Scheduling;
import com.github.ddth.akka.scheduling.tickfanout.MultiNodeQueueBasedTickFanOutActor;
import com.github.ddth.commons.redis.JedisConnector;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.dlock.IDLock;
import com.github.ddth.dlock.impl.redis.RedisDLockFactory;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.QueueSpec;
import com.github.ddth.queue.impl.universal.idint.UniversalRedisQueueFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class QndMultiNodeTickFanOut {

    private static class MyWorker1 extends BaseWorker {
        @Override
        protected CronFormat getScheduling() {
            return CronFormat.parse("*/3 * *");
        }

        @Override
        protected void doJob(TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    @Scheduling("*/5 * *")
    private static class MyWorker2 extends BaseWorker {
        @Override
        protected void doJob(TickMessage tick) throws Exception {
            Date now = new Date();
            System.out.println("{" + self().path() + "}: " + tick.getId() + " / "
                    + DateFormatUtils.toString(now, DateFormatUtils.DF_ISO8601) + " / "
                    + DateFormatUtils.toString(tick.getTimestamp(), DateFormatUtils.DF_ISO8601)
                    + " / " + (now.getTime() - tick.getTimestamp().getTime()));
        }
    }

    public static void main(String[] args) throws Exception {
        try (JedisConnector jedisConnector = new JedisConnector()) {
            jedisConnector.setRedisHostsAndPorts("localhost:6379");
            jedisConnector.init();

            try (RedisDLockFactory dlockFactory = new RedisDLockFactory()) {
                dlockFactory.setJedisConnector(jedisConnector).setLockNamePrefix("dlock-").init();

                try (UniversalRedisQueueFactory queueFactory = new UniversalRedisQueueFactory()) {
                    queueFactory.setJedisConnector(jedisConnector).init();

                    ActorSystem actorSystem = AkkaUtils.createActorSystem("my-actor-system");
                    try {
                        System.out.println("Actor system: " + actorSystem);

                        System.out.println("Actor: "
                                + actorSystem.actorOf(Props.create(MyWorker1.class), "worker1"));
                        System.out.println("Actor: "
                                + actorSystem.actorOf(Props.create(MyWorker2.class), "worker2"));

                        IQueue<?, byte[]> queue = queueFactory.getQueue(new QueueSpec("demo"));
                        System.out.println("Queue: " + queue);

                        IDLock dlock = dlockFactory.createLock("demo");
                        System.out.println("DLock: " + dlock);

                        ActorRef tickFanOut = MultiNodeQueueBasedTickFanOutActor
                                .newInstance(actorSystem, dlock, queue, 1000, 5000);
                        System.out.println(tickFanOut);

                        Thread.sleep(60000);

                        actorSystem.stop(tickFanOut);

                        Thread.sleep(1000);
                    } finally {
                        actorSystem.terminate().value();
                    }
                }
            }
        }
    }

}
