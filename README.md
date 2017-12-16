# project-tron

Publicly hosted on IBM Cloud here: [http://projecttron.mybluemix.net/](http://projecttron.mybluemix.net/)

Bluemix toolchain automatically deploys the current `project-tron/project-tron:master` branch

## Local development

*[If using eclipse IDE]* Generate eclipse files before importing projects into eclipse:

```
./gradlew eclipse
```

Builds the webapp, starts liberty server, and deploys app to server.  Once app is running, opens web browser

```
./gradlew libertyStart open
```

Any code changes that are made in an eclipse environment with auto-build enabled will automatically publish content to the loose application, meaning no server restarts should be required between code changes.

To stop a liberty server, issue the command:

```
./gradlew libertyStop
```
(Note that `libertyRun` and `libertyStart` commands will first invoke `libertyStop`)

Originally cloned from https://github.com/aguibert/coms319-project4
