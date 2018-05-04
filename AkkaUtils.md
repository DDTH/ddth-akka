# ddth-akka: AkkaUtils

Some useful helper methods to work with [Akka](https://akka.io).

Artifact: `com.github.ddth:ddth-akka-core:${ddth-akka-version}`.

Class: `com.github.ddth.akka.AkkaUtils`.

**`public static akka.actor.ActorSystem createActorSystem(String name)`**

Create an `ActorSystem` with default configurations. The "default configurations" is loaded from `com/github/ddth/akka/default-akka-standalone.conf`.

**`public static akka.actor.ActorSystem createActorSystem(String name, com.typesafe.config.Config config)`**

Create an `ActorSystem` with specified configurations.

The config of type `com.typesafe.config.Config` can be loaded from file using `TypesafeConfigUtils` from [`com.github.ddth:ddth-commons-typesafeconfig:${version}`](https://github.com/DDTH/ddth-commons).
