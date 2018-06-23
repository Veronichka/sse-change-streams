# sse-change-streams

## Overview
An experiment project using SSE (server sent events) together with MongoDB 3.6 [change streams](https://docs.mongodb.com/manual/changeStreams/).

Currently the project is in progress but the main part is already implemented. To see how it works check `ControllerIntegrationTest`.

## Prerequisites

* [JDK 8](http://www.oracle.com/technetwork/java/index.html) installed and working
* Building under [Ubuntu Linux](https://www.ubuntu.com/) is supported and recommended
* [MongoDB 3.6+](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/) up and running under port `3000` (configured under `sse-change-streams/src/main/resources/config/application.yml`). Pay attention that you can only open a [change stream](https://docs.mongodb.com/manual/changeStreams/) against replica sets or sharded clusters.

## Building

Type `./gradlew` to build and assemble the service.

