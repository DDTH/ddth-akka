package com.github.ddth.akka.qnd;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.github.ddth.akka.AkkaUtils;

import java.util.Random;

public class QndCreateActorSystem {
    static {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "false");
    }

    static class MyActor extends UntypedAbstractActor {
        private long token;
        private String id;

        public MyActor(String id) {
            this.id = id;
            System.out.println("Constructor: " + id);
        }

        @Override
        public void preStart() throws Exception {
            super.preStart();
            token = System.currentTimeMillis();
            System.out.println("PreStart: " + id + "\t" + token + "\t" + this);
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            System.out.println(id + "\t" + token + "\t" + this + "\tReceived: " + message);
            if (message.equals("3")) {
                throw new RuntimeException("I crash for fun!");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final Random RAND = new Random(System.currentTimeMillis());

        {
            ActorSystem system = AkkaUtils.createActorSystem("my-actor-system");
            System.out.println("ActorSystem: " + system);

            ActorRef ref = system.actorOf(Props.create(MyActor.class, String.valueOf(System.currentTimeMillis())));
            for (int i = 0; i < 10; i++) {
                ref.tell(String.valueOf(i), ActorRef.noSender());
                Thread.sleep(RAND.nextInt(5000));
            }

            system.terminate();
        }
    }
}
