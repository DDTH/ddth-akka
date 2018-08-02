package com.github.ddth.akka.qnd.cluster;

import java.util.concurrent.CompletionStage;

import com.github.ddth.akka.cluster.MasterActor;
import com.github.ddth.akka.cluster.messages.GetLeaderMessage;
import com.github.ddth.akka.cluster.messages.GetLeaderResponseMessage;
import com.github.ddth.akka.cluster.messages.GetNodesMessage;
import com.github.ddth.akka.cluster.messages.IsLeaderMessage;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.UntypedAbstractActor;
import akka.pattern.PatternsCS;

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
        ActorSystem actorSystem1 = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node1.conf", MasterActor.class);
        ActorSystem actorSystem2 = startActorSystem(
                "com/github/ddth/akka/qnd/cluster/akka-cluster-node2.conf", MasterActor.class);
        Thread.sleep(5000);

        {
            ActorSelection master = actorSystem1.actorSelection("/user/MasterActor");
            CompletionStage<Object> future = PatternsCS.ask(master, new GetLeaderMessage("master"),
                    10000);
            System.out.println(future.toCompletableFuture().get());

            future = PatternsCS.ask(master, new GetNodesMessage("master"), 10000);
            System.out.println(future.toCompletableFuture().get());
        }

        {
            ActorSelection master = actorSystem2.actorSelection("/user/MasterActor");
            CompletionStage<Object> future = PatternsCS.ask(master, new GetLeaderMessage("master"),
                    10000);
            System.out.println(future.toCompletableFuture().get());

            GetLeaderResponseMessage msg = (GetLeaderResponseMessage) future.toCompletableFuture()
                    .get();

            future = PatternsCS.ask(master, new IsLeaderMessage("master", msg.node), 10000);
            System.out.println(future.toCompletableFuture().get());
        }

        Thread.sleep(10000);
        actorSystem1.terminate();
        actorSystem2.terminate();
    }
}
