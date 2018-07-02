# ddth-akka: Clustering

Utilities and Helpers to work with [Akka cluster](https://doc.akka.io/docs/akka/2.5/java/index-network.htmlhttps://doc.akka.io/docs/akka/2.5/index-cluster.html).

Artifact: `com.github.ddth:ddth-akka-core:${ddth-akka-version}`.

### Introduction

This library provides handy utility and helper classes to work with [Akka cluster](https://doc.akka.io/docs/akka/2.5/index-cluster.html).

- Package `com.github.ddth.akka.cluster`: Akka cluster support
  - `BaseClusterActor`: Base class to implement Akka cluster actors.
  - `ClusterMemberUtils`: Utility class to manage cluster's member nodes.
  - `DistributedDataUtils`: Utility class to work with cluster's distributed-data.
  - `MasterActor`: Actor that keeps track of nodes within the cluster (work in alignment with `ClusterMemberUtils`).
- Package `com.github.ddth.akka.cluster.scheduling`: Scheduling jobs in Akka clustering mode.
  - `BaseClusterWorker`: Base class to implement cluster workers.
  - `ClusterTickFanOutActor`:  Tick-fan-out actor for Akka clustering mode.
- Package `com.github.ddth.akka.cluster.serialization`:
  - `DdthAkkaSerializer`: An out-of-the-box serializer for actor system in network/cluster mode.

### Usecase: Keep track of cluster nodes

- Create one instance of `com.github.ddth.akka.cluster.MasterActor` per ActorSystem.
- `MasterActor` listens to cluster events and keeps track of cluster member nodes' up/down/join/leave status.
- Helper methods in `com.github.ddth.akka.cluster.ClusterMemberUtils` can be used to get status cluster member nodes:

### Usecase: Scheduling jobs in cluster mode

- Create one instance of `com.github.ddth.akka.cluster.scheduling.ClusterTickFanOutActor` per ActorSystem.
- Create workers: it is recommended to implement cluster workers by extending `BaseClusterWorker`.

Note: scheduling jobs in cluster mode does not require external system as with [scheduling jobs in non-cluster mode](Scheduling.md).
