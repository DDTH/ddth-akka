[![Build Status](https://travis-ci.org/DDTH/ddth-akka.svg?branch=master)](https://travis-ci.org/DDTH/ddth-akka)

# ddth-akka

DDTH's Common Helpers and Utilities for Akka.

Project home:
[https://github.com/DDTH/ddth-akka](https://github.com/DDTH/ddth-akka)

**`ddth-akka` requires Java 8+**


## License

See LICENSE.txt for details. Copyright (c) 2018 Thanh Ba Nguyen.

Third party libraries are distributed under their own licenses.


## Installation

Latest release version: `0.1.3`. See [RELEASE-NOTES.md](RELEASE-NOTES.md).

Maven dependency: if only a sub-set of `ddth-akka` functionality is used, choose the corresponding
dependency artifact(s) to reduce the number of unused jar files.

*ddth-akka-core*:

```xml
<dependency>
    <groupId>com.github.ddth</groupId>
    <artifactId>ddth-akka-core</artifactId>
    <version>0.1.3</version>
</dependency>
```


## Usage

### AkkaUtils

Some useful helper methods to work with [Akka](https://akka.io). See [AkkaUtils.md](AkkaUtils.md).


### Clustering

Easier to work with [Akka cluster](https://doc.akka.io/docs/akka/2.5/index-cluster.html). See [Clustering.md](Clustering.md).


### Scheduling

Scheduling jobs with [Akka](https://akka.io). See [Scheduling.md](Scheduling.md).
