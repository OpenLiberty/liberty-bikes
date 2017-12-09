# project-tron

Cloned from https://github.com/aguibert/coms319-project4

Original readme.md:

coms319-project4
================
## Setup Instructions
1. You will need Eclipse for Java EE developers (normal eclipse won't do)
2. Go to wasdev.net, download the latest beta image and eclipse tooling https://developer.ibm.com/wasdev/downloads/liberty-profile-beta/
  1. Follow the steps for "Installing with the Eclipse tools beta" to install the tooling
  2. Follow the steps for "Installing from the command line" to install the runtime
3. Once you clone the repository, import existing projects into your workspace
4. Create the server runtime:
  1. In eclipse, File->Server->Runtime Environments->Add... Websphere Application Server Liberty Profile
  2. Next, browse for the location where you extracted your Wepshere runtime, then click Finish
5. In the servers view (select )
6. Name the server "project4-server"
7. Add the coms319.team10.project4 resource to the server, then click finish.
8. In the "server" tab, you should have created a "project4-server" server, you can start it by selecting it and pressing the green play button in the server tab.