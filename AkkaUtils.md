# ddth-akka: AkkaUtils

Some useful helper methods to work with [Akka](https://akka.io).

Artifact: `com.github.ddth:ddth-akka-core:${ddth-akka-version}`.

Class: `com.github.ddth.akka.AkkaUtils`.

**`ActorSystem createActorSystem(String name)`**

Create an `ActorSystem` with default configurations. The "default configurations" is loaded from `com/github/ddth/akka/default-akka-standalone.conf`.

**`ActorSystem createActorSystem(String name, Config config)`**

Create an `ActorSystem` with specified configurations.

The config of type `com.typesafe.config.Config` can be loaded from file using `TypesafeConfigUtils` from [`com.github.ddth:ddth-commons-typesafeconfig:${version}`](https://github.com/DDTH/ddth-commons).

**`Object simpleAsk(ActorSystem actorSystem, ActorRef target, Object request, Duration timeout)`**

Perform a simple ask-response pattern on the target actor ref.

**`Collection<ActorRef> selectActors(ActorSystem actorSystem, ActorPath path, Duration duration)` and `Collection<ActorRef> selectActors(ActorSystem actorSystem, String path, Duration duration)`**

Find all actors matching a specified path.
