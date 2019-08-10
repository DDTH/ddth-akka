package com.github.ddth.akka.qnd.cluster;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.UntypedAbstractActor;
import akka.pattern.Patterns;
import com.github.ddth.akka.cluster.MasterActor;
import com.github.ddth.akka.cluster.messages.GetLeaderMessage;
import com.github.ddth.akka.cluster.messages.GetLeaderResponseMessage;
import com.github.ddth.akka.cluster.messages.GetNodesMessage;
import com.github.ddth.akka.cluster.messages.IsLeaderMessage;
import com.github.ddth.commons.utils.TypesafeConfigUtils;
import com.typesafe.config.Config;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.File;

public class QndClusterLeaderManager extends BaseQnd {
    static class AskActor extends UntypedAbstractActor {

        private ActorSelection target;
        private Object msg;

        public AskActor(ActorSelection target, Object msg) {
            this.target = target;
            this.msg = msg;
        }

        @Override
        public void preStart() throws Exception {
            super.preStart();

            System.out.println(self());
            target.tell(msg, self());
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            System.out.println("-== " + message);
        }

    }

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem1, actorSystem2;

        System.err.println("Starting actor system 1...");
        {
            File configFile1 = new File(
                    "ddth-akka-core/src/test/java/com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf");
            Config config1 = TypesafeConfigUtils.loadConfig(configFile1, true);
            actorSystem1 = startActorSystem(config1, MasterActor.class);
        }

        System.err.println("Starting actor system 2...");
        {
            File configFile2 = new File(
                    "ddth-akka-core/src/test/java/com/github/ddth/akka/qnd/cluster/akka-cluster-node2.conf");
            Config config2 = TypesafeConfigUtils.loadConfig(configFile2, true);
            actorSystem2 = startActorSystem(config2, MasterActor.class);
        }

        Thread.sleep(1234);

        {
            System.out.println("======================================================================");
            Future<?> f;
            ActorSelection master = actorSystem1.actorSelection("/user/" + MasterActor.class.getSimpleName());

            f = Patterns.ask(master, new GetLeaderMessage("master"), 10000);
            System.out.println(f.result(Duration.Inf(), null));

            f = Patterns.ask(master, new GetNodesMessage("master"), 10000);
            System.out.println(f.result(Duration.Inf(), null));
        }

        {
            System.out.println("======================================================================");
            Future<?> f;
            ActorSelection master = actorSystem2.actorSelection("/user/" + MasterActor.class.getSimpleName());

            f = Patterns.ask(master, new GetLeaderMessage("master"), 10000);
            GetLeaderResponseMessage msg = (GetLeaderResponseMessage) f.result(Duration.Inf(), null);
            System.out.println(msg);

            f = Patterns.ask(master, new IsLeaderMessage("master", msg.node), 10000);
            System.out.println(f.result(Duration.Inf(), null));
        }

        System.out.println("======================================================================");

        Thread.sleep(2345);
        actorSystem1.terminate();
        actorSystem2.terminate();
    }
}
