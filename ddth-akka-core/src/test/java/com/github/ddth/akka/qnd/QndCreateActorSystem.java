package com.github.ddth.akka.qnd;

import java.util.Random;

import com.github.ddth.akka.AkkaUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;

public class QndCreateActorSystem {

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
            System.out.println(id + "\t" + token + "\t" + this);
            System.out.println("Received: " + message);
            if (message.equals("3")) {
                throw new RuntimeException("I crash for fun!");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final Random RAND = new Random(System.currentTimeMillis());

        {
            ActorSystem system = AkkaUtils.createActorSystem("my-actor-system");
            System.out.println(system);

            ActorRef ref = system.actorOf(
                    Props.create(MyActor.class, String.valueOf(System.currentTimeMillis())));
            for (int i = 0; i < 10; i++) {
                ref.tell(String.valueOf(i), ActorRef.noSender());
                Thread.sleep(RAND.nextInt(5000));
            }

            system.terminate();
        }

        // {
        // Config config = TypesafeConfigUtils.loadConfig(
        // "src/test/java/com/github/ddth/akka/qnd/akka-standalone.conf", true);
        // ActorSystem system = AkkaUtils.createActorSystem("my-actor-system",
        // config);
        // System.out.println(system);
        // print(system.settings(), "config2.conf");
        // system.terminate();
        // }
        //
        // {
        // Config config = TypesafeConfigUtils.loadConfig(
        // QndCreateActorSystem.class.getClassLoader(),
        // "com/github/ddth/akka/default-akka-standalone.conf", true);
        // ActorSystem system = AkkaUtils.createActorSystem("my-actor-system",
        // config);
        // System.out.println(system);
        // print(system.settings(), "config3.conf");
        // system.terminate();
        // }
    }

}
