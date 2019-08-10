# ddth-akka release notes

## 1.0.0 - 2019-08-09

- Migrate to `Java 11`.


## 0.1.4.1 - 2018-08-20

- Bug fixes & enhancements: `BaseActor`, `BaseClusterActor`, `BaseWorker` and `BaseClusterWorker` obey `handleMessageAsync` setting.


## 0.1.4 - 2018-08-02

- New class `BaseMessage`: base class to implement other Akka messages.
- `BaseActor`: message handlers can be invoked either asynchronously or synchronously.
- `ClusterMemberUtils`: new methods `resetNodes` and `getNodes(String)`.
- `MasterActor` & package `com.github.ddth.akka.cluster.messages`:
  - Get cluster's state via messages
  - Update cluster state when `MasterActor` initializes
- Other bug fixes & enhancements.


## 0.1.3 - 2018-07-02

- Add [Akka cluster](https://doc.akka.io/docs/akka/2.5/index-cluster.html) support, see [Clustering.md](Clustering.md).
- `TickFanOutActor`:
  - switch to Akka scheduling instead of `Actor timer`
  - renew "clock" every 24h to cope with long-term scheduling


## 0.1.2 - 2018-06-15

- Upgrade to `ddth-dlobk:0.1.2`:
  - `Scheduling`: more options to cope with `ddth-dlobk:0.1.2`.
  - `BaseWorker`: now support lock "fairness".
- Bug fixes & Enhancements.


## 0.1.1.2 - 2018-06-02

- Bug fixes & Enhancements.


## 0.1.1 - 2018-05-30

- Removed `MultiNodeQueueBasedTickFanOutActor`, replaced with `MultiNodePubSubBasedTickFanOutActor`:
  - At one specific time, only one `MultiNodePubSubBasedTickFanOutActor` creates and publishes tick-message.
    But the tick-message is received by all workers on all nodes. It's up to the worker to decide the coordination.
- `BaseWorker` and `Scheduling` rework:
  - New enum class `WorkerCoordinationPolicy`
  - Migrate work flow from queue-based to pub/sub-based.
- Update dependency libs.


## 0.1.0.1 - 2018-05-07

- Upgrade `ddth-commons` to `v0.9.1.3`.
- `MultiNodeQueueBasedTickFanOutActor`: add 2 new attributes:
  - `queuePollSleepMs`: sleep time between queue poll in milliseconds (default value `1000`).
  - `dlockTimeMs`: d-lock time in milliseconds (default value `5000`).


## 0.1.0 - 2018-05-07

- First release.
