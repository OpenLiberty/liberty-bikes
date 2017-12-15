# project-tron

Publicly hosted on IBM Cloud here: [http://projecttron.mybluemix.net/](http://projecttron.mybluemix.net/)

Bluemix toolchain automatically deploys the current `project-tron/project-tron:master` branch

## To run locally

Builds the webapp, starts liberty server, and deploys app to server.  Once app is running, opens web browser

```
./gradlew libertyStart openBrowser
```

To stop a liberty server, issue the command:

```
./gradlew libertyStop
```
(Note that `libertyRun` and `libertyStart` commands will first invoke `libertyStop`)

Originally cloned from https://github.com/aguibert/coms319-project4
