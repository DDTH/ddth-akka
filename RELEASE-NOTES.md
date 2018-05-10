# ddth-akka release notes

## 0.1.0.1 - 2018-05-07

- Upgrade `ddth-commons` to `v0.9.1.3`.
- `MultiNodeQueueBasedTickFanOutActor`: add 2 new attributes:
  - `queuePollSleepMs`: sleep time between queue poll in milliseconds (default value `1000`).
  - `dlockTimeMs`: d-lock time in milliseconds (default value `5000`).


## 0.1.0 - 2018-05-07

- First release.
