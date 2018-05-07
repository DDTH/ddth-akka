package com.github.ddth.akka.qnd;

import java.io.FileWriter;
import java.io.IOException;

import com.github.ddth.akka.AkkaUtils;

import akka.actor.ActorSystem;
import akka.actor.ActorSystem.Settings;

public class QndCreateActorSystem {

    private static void print(Settings settings, String filenname) throws IOException {
        try (FileWriter writer = new FileWriter(filenname)) {
            String[] lines = settings.toString().split("[\r\n]+");
            for (String line : lines) {
                if (!line.matches("^\\s+#.*$")) {
                    System.out.println(line);
                    writer.write(line + "\n");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        {
            ActorSystem system = AkkaUtils.createActorSystem("my-actor-system");
            System.out.println(system);
            print(system.settings(), "config1.conf");
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
