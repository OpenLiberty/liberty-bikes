# Liberty Bikes

![Image of Liberty Bikes game](https://user-images.githubusercontent.com/5427967/40553647-f7ee3c80-6008-11e8-921a-d5295c5a90af.png)

Publicly hosted on IBM Cloud here: [http://libertybikes.mybluemix.net/](http://libertybikes.mybluemix.net/)

Bluemix toolchain automatically deploys the current `liberty-bikes/liberty-bikes:master` branch

## Run it locally

Builds all microservice applications and deploys them to locally running liberty servers, then opens the UI.

```
./gradlew start frontend:open
```

For a local setting, use single-party mode:
```
./gradlew start frontend:open -DsingleParty=true
```

Any code changes that are made in an eclipse environment with auto-build enabled will automatically publish content to the loose application, meaning no server restarts should be required between code changes.

To stop all liberty servers, issue the command:

```
./gradlew stop
```

# Technologies used

- Java EE 8
  - CDI 2.0 (auth-service, game-service, player-service)
  - [EE Concurrency](#ee-concurrency) (game-service, player-service)
  - JAX-RS 2.1 (auth-service, game-service, player-service)
  - JNDI (auth-service, game-service, player-service)
  - [JSON-B](#json-b) (game-service, player-service)
  - WebSocket 1.1 (game-service)
- MicroProfile 1.3
  - Config (auth-service, game-service, player-service)
  - JWT (auth-service, game-service, player-service)
  - [Rest Client](#microprofile-rest-client) (game-service)
- Angular 6 (frontend)
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

By default, JSON-B will expose any `public` members as wel as public `getXXX()`, this includes other objects such as the `Set<Player> players` field.  The resulting class gets serialized into something like this:

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
    classpath 'net.wasdev.wlp.gradle.plugins:liberty-gradle-plugin:2.3'
  }
}
```

To control the Liberty distribution, we simply specify a dependency:

```groovy
dependencies {
    libertyRuntime group: 'com.ibm.websphere.appserver.runtime', name: 'wlp-webProfile7', version: '+'
}
```

Or, if we want to use a Beta image instead of an official GA'd image, we specify a URL in the `liberty.install` task instead of as a runtime dependency:

```groovy
liberty {
  install {
    // use 1 liberty install for the whole repo
    baseDir = rootProject.buildDir
    runtimeUrl = "https://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/beta/wlp-beta-2018.5.0.0.zip"
  }
}
```

And lastly, we added some convenience Gradle tasks to make our life a bit easier (shorter names for less typing, always run unit tests before starting the server, and always stop the server before trying to start it!).

```groovy
libertyStart.dependsOn 'libertyStop', 'test'

task start { dependsOn 'libertyStart' }
task stop  { dependsOn 'libertyStop'  }
```

## Continuous Delivery

Early on we set up a build pipeline on IBM Cloud that we pointed at this GitHub repository.  Every time a new commit is merged into the `master` branch, the pipeline kicks off a new build and redeploys all of the services.  The average time from pressing merge on a PR to having the changes live on libertbikes.mybluemix.net is around 20 minutes.

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
