package com.github.ddth.akka.qnd;

import akka.actor.*;
import com.github.ddth.akka.AkkaUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Random;

public class QndSelector {
    static class EchoActor extends UntypedAbstractActor {
        @Override
        public void onReceive(Object message) throws Throwable {
            ActorRef sender = sender();
            if (sender != ActorRef.noSender()) {
                sender.tell(message, ActorRef.noSender());
            } else {
                unhandled(message);
            }
        }
    }

    static class MyActor extends UntypedAbstractActor {
        private long token;
        private String id;

        public MyActor(String id) {
            this.id = id;
            System.out.println("\tConstructor: " + id);
        }

        @Override
        public void preStart() throws Exception {
            super.preStart();
            token = System.currentTimeMillis();
            System.out.println("\tPreStart: " + id + "\t" + token + "\t" + this);
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            System.out.println("\t" + id + "\t" + token + "\t" + this + "\tReceived: " + message);
        }
    }

    private static class MySelectorActor extends UntypedAbstractActor {
        private ActorSelection actorSelection;

        public MySelectorActor(String path) {
            actorSelection = context().actorSelection(path);
            actorSelection.tell(new Identify(System.currentTimeMillis()), self());
        }

        public MySelectorActor(ActorPath path) {
            actorSelection = context().actorSelection(path);
            actorSelection.tell(new Identify(System.currentTimeMillis()), self());
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            System.out.println("MySelectorActor received: " + message);
        }
    }

    public static void main(String[] args) throws Exception {
        final Random RAND = new Random(System.currentTimeMillis());

        ActorSystem system = AkkaUtils.createActorSystem("my-actor-system");
        try {
            System.out.println("ActorSystem: " + system);

            ActorRef echoRef = system.actorOf(Props.create(EchoActor.class), "echo");
            Object echoResponse = AkkaUtils.simpleAsk(system, echoRef, "This a a message", Duration.ofMillis(1000));
            System.out.println("Echo response: " + echoResponse);

            ActorRef myActor = system.actorOf(Props.create(MyActor.class, "MyActor"), "myactor");

            String path = "/user/*";
            long t = System.currentTimeMillis();
            Collection<ActorRef> result = AkkaUtils.selectActors(system, path, Duration.ofMillis(7890));
            long d = System.currentTimeMillis() - t;
            System.out.println("Select actors[" + path + "] (" + d + " ms): " + result);
            if (result != null) {
                result.forEach(ref -> {
                    System.out.println("\t" + ref.path());
                });
            }

            Thread.sleep(5000);
        } finally {
            system.terminate();
        }
    }
}
