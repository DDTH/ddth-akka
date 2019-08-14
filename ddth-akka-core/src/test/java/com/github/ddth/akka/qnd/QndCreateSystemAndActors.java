package com.github.ddth.akka.qnd;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.github.ddth.akka.AkkaUtils;
import com.github.ddth.akka.BaseActor;
import com.github.ddth.commons.utils.DateFormatUtils;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class QndCreateSystemAndActors {
    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    final static String DF = "HH:mm:ss.SSS";

    static class MyActor1 extends UntypedAbstractActor {
        @Override
        public void onReceive(Object msg) {
            String str = MessageFormat
                    .format("At {0}, [{1}] sent [{2}] a message: {3}", DateFormatUtils.toString(new Date(), DF),
                            sender().path().name(), self().path().name(), msg);
            System.out.println(str);
        }
    }

    static class MyActor2 extends BaseActor {
        public MyActor2(boolean handleMessageAsync) {
            setHandleMessageAsync(handleMessageAsync);
        }

        @Override
        protected void initActor() throws Exception {
            super.initActor();
            addMessageHandler(String.class, msg -> {
                String str = MessageFormat
                        .format("At {0}, [{1}] sent [{2}/{3}] a message: {4}", DateFormatUtils.toString(new Date(), DF),
                                sender().path().name(), self().path().name(), isHandleMessageAsync(), msg);
                System.out.println(str);
            });
        }

        @Override
        protected Collection<Class<?>> channelSubscriptions() {
            return Collections.singleton(String.class);
        }
    }

    public static void main(String[] args) throws Exception {
        final Random RAND = new Random(System.currentTimeMillis());
        ActorSystem system = AkkaUtils.createActorSystem("my-actor-system");
        try {
            ActorRef[] actors = new ActorRef[3];
            {
                actors[0] = system.actorOf(Props.create(MyActor1.class), "Actor1");
                actors[1] = system.actorOf(Props.create(MyActor2.class, true), "Actor2");
                actors[2] = system.actorOf(Props.create(MyActor2.class, false), "Actor3");
            }
            for (int i = 0; i < 10; i++) {
                int index = RAND.nextInt(actors.length);
                actors[index].tell("Message " + i, system.guardian());
                Thread.sleep(RAND.nextInt(3000));
            }
        } finally {
            system.terminate();
        }
    }
}
