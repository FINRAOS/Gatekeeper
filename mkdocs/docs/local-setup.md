## Local Setup

We are initally providing a local run of the gatekeeper application, in the coming months we will provide more details on getting the Gatekeeper Application deployed into AWS.

### Prerequisites

Gatekeeper requires the following tools to be installed and on your $PATH variable:

1. Java 8+
2. Maven 3+
3. NPM 3+
4. Docker

### Mac + Linux environments (1-Command Setup)
Currently we provide a shell script that builds all of the app code / containers that comprise Gatekeeper. Currently we only support this for Linux and Mac environments. All you have to do is provide the following environment variables

1. **AWS_DIRECTORY**: the location on your computer with your AWS Credentials usually it's <USER_HOME>/.aws
2. **http_proxy**: if behind a corporate proxy set the proxy to this variable. (defaults to blank if not provided)

Once you have these set, simply run **start.sh** and the script will build all the gatekeeper code/containers + bring up gatekeeper locally.

Hit gatekeeper at the following locations to see the different User/Roles in action:

https://localhost:443 => Regular User

https://localhost:444 => Ops/Support User

https://localhost:445 => Approver

### Windows 10 users

If you're a windows 10 user we don't have a script for you unfortunately, but that doesn't mean you can't run gatekeeper locally. 

Make sure you have the following environment variables set:

1. **AWS_DIRECTORY**: the location on your computer with your AWS Credentials usually it's <USER_HOME>/.aws
2. **http_proxy**: if behind a corporate proxy set the proxy to this variable. you need to set it to blank if not provided

Run the following commands:

```bash
#cd to containers folder and build the base containers
docker-compose build
#cd to demo-services and build the fake-account-service
mvn clean install
#cd to services directory and build the gatekeeper backend services
mvn clean install
#cd to the ui directory and build the gatekeeper UI code
npm run win-build
#cd to the main directory and build the gatekeeper containers
docker-compose -f local-docker-compose.yml build
#bring the gatekeeper up with the following command
docker-compose -f local-docker-compose.yml up
```
Hit gatekeeper at the following locations to see the different User/Roles in action:

https://localhost:443 => Regular User

https://localhost:444 => Ops/Support User

https://localhost:445 => Approver