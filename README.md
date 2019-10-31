# Liberty Bikes
[![Build Status](https://travis-ci.org/OpenLiberty/liberty-bikes.svg?branch=master)](https://travis-ci.org/OpenLiberty/liberty-bikes)


![Image of Liberty Bikes game](https://user-images.githubusercontent.com/1577201/47185063-0d307c00-d2f2-11e8-87f5-997ecf22c3d4.png)

Publicly hosted on IBM Cloud here: [http://libertybikes.mybluemix.net/](http://libertybikes.mybluemix.net/)

Bluemix toolchain automatically deploys the current `liberty-bikes/liberty-bikes:master` branch

## How to setup locally

### Prereqs:

- [Java 8 or newer](https://adoptopenjdk.net/index.html?variant=openjdk8&jvmVariant=openj9). Java must also be on the `$PATH`. If you can run `java -version` from a terminal window, then Java is on your `$PATH`.
- Have [Git installed](https://git-scm.com/downloads)
- [*Optional*] Have [Docker installed](https://hub.docker.com/?overlay=onboarding) if you want to use the real database or Grafana dashboard.

### Clone and run

First, clone this github repo with the following commands:

```
git clone git@github.com:OpenLiberty/liberty-bikes.git
cd liberty-bikes
```

If you have a Github account, press the "Fork" button in the top right corner of this web page to fork the repository.

Next, build and deploy all microservice applications on locally running liberty servers, then open the game in a web browser. If you are on Windows, you may need to manually open the game in a web browser at http://localhost:12000

```
./gradlew start frontend:open
```

Any code changes that are made in an IDE with auto-build enabled will automatically publish content to the loose application, meaning no server restarts should be required between code changes.

### Optional Docker steps

By default, the player-service stores player registration and stats in-memory. To use a real database, you can start a PostgreSQL docker container with this script:

```
./startDB.sh
```

To start the monitoring services, you must have Docker installed. They can be started with:

```
./startMonitoring.sh
```

### How to shut everything down cleanly

To stop all liberty servers, issue the command:

```
./gradlew stop
```

## Run it locally in containers

(Requires docker and docker-compose to be installed. The Docker daemon must be running.)

Normally you get better performance running services outside of containers (aka bare metal), but if you want to build and run all of the containers locally, run the command: 

```
./gradlew dockerStart
```

To stop and remove the containers, use:

```
./gradlew dockerStop
```

# Technologies used

- Java EE 8
  - CDI 2.0 (auth-service, game-service, player-service)
  - [EE Concurrency](#ee-concurrency) (game-service, player-service)
  - JAX-RS 2.1 (auth-service, game-service, player-service)
  - JNDI (auth-service, game-service, player-service)
  - [JSON-B](#json-b) (game-service, player-service)
  - WebSocket 1.1 (game-service)
- MicroProfile 2.2 
  - Config (auth-service, game-service, player-service)
  - JWT (auth-service, game-service, player-service)
  - [Rest Client](#microprofile-rest-client) (game-service)
  - [OpenAPI](#microprofile-openapi) (auth-service, game-service, player-service)
  - [Metrics](#monitoring) (auth-service, game-service, player-service, frontend)
- Angular 7 (frontend)
- Prometheus for metric collection
- Grafana for metric visualization
- Gradle build
  - [Liberty Gradle Plugin](#liberty-gradle-plugin)
- [IBM Cloud Continuous Delivery Pipeline](#continuous-delivery)


## JSON-B 

Several of the backend entities need to be represtented as JSON data so they can be sent to the frontend via websocket, these include objects like `GameBoard`, `Obstacle`, and `Player`.  Using POJOs and the occasional `@JsonbTransient` annotation, we used JSON-B to transform Java objects to JSON data.

```java
public class GameBoard {

    @JsonbTransient
    public final short[][] board = new short[BOARD_SIZE][BOARD_SIZE];

    public final Set<Obstacle> obstacles = new HashSet<>();
    public final Set<MovingObstacle> movingObstacles = new HashSet<>();
    public final Set<Player> players = new HashSet<>();

    // ...
}
```

By default, JSON-B will expose any `public` members as well as public `getXXX()`, this includes other objects such as the `Set<Player> players` field.  The resulting class gets serialized into something like this:

```json
{
  "movingObstacles" : [ 
    { "height":12, "width":11, "x":13, "y":14 }
  ],
  "obstacles" : [
    { "height":2, "width":1, "x":3, "y":4 }
  ],
  "players" : [    
    { "id":"1234", "name":"Bob", "color":"#f28415", "status":"Connected", "alive":true, "x":9, "y":9, "width":3, "height":3, "direction":"RIGHT" }
  ]
}
```

## MicroProfile Rest Client

Each of the 3 backend microservices in Liberty Bikes (auth, game, and player) exposed a REST API.  In most cases the frontend would call the backend REST services, but sometimes the backend services had to call each other.  

For example, when a game is over, the game service makes REST calls to the player service to update the player statistics.  To accomplish this, the game-service simply defines a POJI (plain old Java Interface) that represents the player-service API it cares about, including the data model:

```java
import javax.ws.rs.*;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
@Path("/")
public interface PlayerService {

    @GET
    @Path("/player/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Player getPlayerById(@PathParam("playerId") String id);

    @POST
    @Path("/rank/{playerId}/recordGame")
    public void recordGame(@PathParam("playerId") String id, @QueryParam("place") int place);

}

public class Player {
    public String id;
    public String name;
}
```

Then, to use the Rest Client in the game service, we simply inject the interface and an implementation is proxied for us:

```java
@ServerEndpoint("/round/ws/{roundId}")
public class GameRoundWebsocket {

    @Inject
    @RestClient
    PlayerService playerSvc;

    @Inject
    GameRoundService gameSvc;
    
    private final static Jsonb jsonb = JsonbBuilder.create();
    
    @OnMessage
    public void onMessage(@PathParam("roundId") final String roundId, String message, Session session) {
        InboundMessage msg = jsonb.fromJson(message, InboundMessage.class);
        GameRound round = gameSvc.getRound(roundId);
        // ...
        Player playerResponse = playerSvc.getPlayerById(msg.playerJoinedId);
        round.addPlayer(session, msg.playerJoinedId, playerResponse.name, msg.hasGameBoard);
        // ...
    }
}      
```

The only non-Java part about MP Rest Client is the need to specify the base path to the service via JVM option.  This is easy enough to do in the build scripting, and easily overridable for cloud environments:

```groovy
liberty {
  server {
    name = 'game-service'
    jvmOptions = ['-Dorg.libertybikes.restclient.PlayerService/mp-rest/url=http://localhost:8081/']
  }
}
```

## Microprofile OpenAPI

Especially while developing new Rest APIs locally, it is useful to inspect the exposed APIs and test them out manually. Simply by enabling the `mpOpenAPI-1.0` feature in server.xml (no application changes needed), all JAX-RS endpoints will be exposed in an interactive web UI.

Here is a snapshot of what the player-service view looks like:

![Image of MP OpenAPI web ui](https://user-images.githubusercontent.com/5427967/47033512-a87ef100-d13a-11e8-827d-375e0f1c4cae.png)

## EE Concurrency

Executors from Java SE are very easy to use, and the "Managed" equivalent Executors in EE Concurrency lets you use all of the SE functionality with the added benefit of running the work on threads that are A) managed by the application server and B) have the proper thread context metadata to perform "EE type" operations such as CDI injections and JNDI lookups.

```java
System.out.println("Scheduling round id=" + roundId + " for deletion in 5 minutes");
exec.schedule(() -> {
    allRounds.remove(roundId);
    System.out.println("Deleted round id=" + roundId);
}, 5, TimeUnit.MINUTES);
```

## Liberty Gradle Plugin

Liberty Bikes can be built and run with a single command and no prereqs thanks to Gradle and the Liberty Gradle Plugin! With these build tools we can easily control a bunch of things:
- Downloading and "installing" Liberty
- Managing build and runtime dependencies (i.e. compile-time classpath and jars that get packaged inside the WAR applications)
- Starting and stopping one or more Liberty servers

To get the Liberty gradle plugin, we add this dependency:

```groovy
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'net.wasdev.wlp.gradle.plugins:liberty-gradle-plugin:2.6.5'
  }
}
```

To control the Liberty distribution, we simply specify a dependency:

```groovy
dependencies {
    libertyRuntime group: 'io.openliberty', name: 'openliberty-runtime', version: '[19.0.0.5,)'
}
```

Or, if we want to use a Beta image instead of an official GA'd image, we specify a URL in the `liberty.install` task instead of as a runtime dependency:

```groovy
liberty {
  install {
    runtimeUrl = "https://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/beta/wlp-beta-2018.5.0.0.zip"
  }
}
```

## Monitoring

If you run Liberty Bikes in a container environment using `./gradlew dockerStart`, a Prometheus and Grafana instance will be started and preconfigured for monitoring the 4 Liberty Bikes microservices.

If you are running locally, you can open a browser to http://localhost:3000 and login with the username/password of `admin/admin` (respectively). The dashboard looks something like this:

![Image of Grafana dashboard](https://user-images.githubusercontent.com/5427967/59791807-807ef900-9298-11e9-96fc-6071c85cf865.png)

The above shapshot shows basic data such as:
- Service Health: Green/Red boxes for up/down respectively
- System info: CPU load and memory usage
- Current stats:
  - Number of players in queue
  - Number of players playing a game
  - Total actions/sec of players
- Overall stats:
  - Total number of logins
  - Total number of games played

Any application-specific stats can be collected using MicroProfile Metrics. For example, to collect number of player logins, we added the following code to our `createPlayer` method:

```java
    @Inject
    private MetricRegistry registry;

     private static final Metadata numLoginsCounter = new Metadata("num_player_logins", // name
                    "Number of Total Logins", // display name
                    "How many times a user has logged in.", // description
                    MetricType.COUNTER, // type
                    MetricUnits.NONE); // units

    @POST
    @Produces(MediaType.TEXT_HTML)
    public String createPlayer(@QueryParam("name") String name, @QueryParam("id") String id) {
      // ...
      registry.counter(numLoginsCounter).inc();
      // ...
    }
```


## Continuous Delivery

Early on we set up a build pipeline on IBM Cloud that we pointed at this GitHub repository.  Every time a new commit is merged into the `master` branch, the pipeline kicks off a new build and redeploys all of the services.  The average time from pressing merge on a PR to having the changes live on libertybikes.mybluemix.net is around 20 minutes.

The pipeline UI looks like this in our dashboard:

![Image of build pipeline](https://user-images.githubusercontent.com/5427967/40152561-41fa1c46-594b-11e8-98b1-3f9f0f0c6472.PNG)

The pipeline consists of 2 stages: Build and Deploy.

The build stage simply points at the GitHub repository URL, and has a little bit of shell scripting where we define how to build the repo:

```bash
#!/bin/bash
export JAVA_HOME=~/java8
./gradlew clean build libertyPackage -Denv_mode=prod
```

For the deployment stage, each microservice gets its own step in the stage.  We could also split the microservices into separate stages (or even different pipelines) if we didn't always want to redeploy all microservices.  Like the build stage, the deploy stage has a little bit of shell scripting at each step:

```bash
#!/bin/bash

# Unzip the archive we receive as build input
cd game-service/build/libs
unzip game-service.zip -d game-service

# Set some Cloud Foundry env vars (use the latest WAS Liberty beta)
cf set-env "${CF_APP}" IBM_LIBERTY_BETA true
cf set-env "${CF_APP}" JBP_CONFIG_LIBERTY "version: +"

# Override the player-service URL for MP Rest Client on game-service
echo "-Dorg.libertybikes.restclient.PlayerService/mp-rest/url=\
http://player-service.mybluemix.net/" > game-service/wlp/usr/servers/game-service/jvm.options

# Push the entire server directory into Cloud Foundry
cf push "${CF_APP}" -p "game-service/wlp/usr/servers/game-service"
```

Originally cloned from https://github.com/aguibert/coms319-project4
