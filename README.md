# project-tron

Publicly hosted on IBM Cloud here: [http://projecttron.mybluemix.net/](http://projecttron.mybluemix.net/)

Bluemix toolchain automatically deploys the current `project-tron/project-tron:master` branch

## Local development

Builds all microservice applications and deploys them to locally running liberty servers, then opens the UI.

```
./gradlew start frontend:open
```

Any code changes that are made in an eclipse environment with auto-build enabled will automatically publish content to the loose application, meaning no server restarts should be required between code changes.

To stop all liberty servers, issue the command:

```
./gradlew stop
```
(Note that `libertyRun` and `libertyStart` commands will first invoke `libertyStop`)

Originally cloned from https://github.com/aguibert/coms319-project4
