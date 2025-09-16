# MassBank of North America
The MoNA application can be found at https://mona.fiehnlab.ucdavis.edu/ or https://massbank.us/

## Getting Started:
- It is highly recommended to use Linux for development (Preferably Ubuntu LTS 18.04 or later)
- Ensure you have the following installed:
  - IntelliJ IDEA Ultimate
  - Maven 3 (Should come with IntelliJ)
  - Angular CLI 10.2.3
  - Docker and Docker Compose
  - AWS CLI (for ECR to pull docker images)
  - NVM (Node Version Manager)
  - NPM (Node Package Manager)
  - Corsproxy (`npm install -g corsproxy` while using npm version 8)
  - Java 17 
  - Scala 2.13 (Installed in IntelliJ)
- Gain Access to the following
  - Private mona-config repo
    - You'll need a GitHub Personal Access Token with access to the repo (https://github.com/settings/tokens)
    - Then open your `~/.bash_rc` and set 'GIT_USER' and 'GIT_PASS' to your GitHub username and
      new personal access token. This allows the discovery service to work as intended
    - Note: You'll need to re-configure this token every time it expires (yearly)
  - An AWS account with permissions to push and pull from ECR. Store credentials under `~/.aws/`
  - Admin access to this public mona repo
  - You'll need a settings.xml file for your maven 'User Settings File' this will include the nexus profile
    and all the necessary repos for pulling down libraries. Ask a developer on the team for this config
    and store the file under `~/.m2`

## Starting Development:
- There are three scripts in the root of the project to easily start development
  - `./start_docker_dev.sh`
    - Starts all necessary docker microservices
  - `./start_corsproxy.sh`
    - Solves CORS issues between the frontend and the microservices
  - `./start_frontend.sh`
    - Starts the Angular 10 frontend with ng serve for live development at localhost:9090
  - Note: To use the awslogging driver in the docker compose files, you need to configure your system's docker installation with proper AWS credentials that have cloudwatch permissions
    - For example, if you have Docker installed with apt, you would run `sudo systemctl edit docker`
    - For Docker installed with snap, you would run `sudo systemctl edit snap.docker.dockerd.service`
    - Then enter the credentials as:  
      1 `[Service]`  
      2 `Environment="AWS_ACCESS_KEY_ID=<id>"`  
      3 `Environment="AWS_SECRET_ACCESS_KEY=<secret>"`  

## Production Deployment:
- MoNA's production instance is hosted on the Gose server using Docker Compose
  - Ask a team member for access to Gose
- Ensure you have your AWS credentials and GitHub PAT set up on Gose 
- Find where the docker compose file is located using `docker compose ls`
  - Navigate to the directory
  - Bring down the application with `docker compose -f docker-compose-prod.yml down`
  - Bring the application back up with `docker compose -f docker-compose-prod.yml up -d`
  - Pull the newest images from ECR with `docker-compose -f docker-compose-prod.yml pull`
    - Pulling the newest images does not affect the current deployment until `docker compose up` is run again
    - Once you have the new image(s) tagged as 'prod' on Gose, you can run `docker compose -f docker-compose-prod.yml up -d` to recreate the containers with updated images

## Generate New Docker Images:
- Make sure the following Maven profiles are selected (and only those): nexus, scala, docker
- The following services can be built into docker images: discovery, bootstrap, webhooks-server, curation-scheduler,
  repository, persistence-server, auth-server, similarity, proxy, download-scheduler, curation-runner, and statistics-server
  - The proxy image is what handles the frontend, so any frontend changes only need the proxy image to be rebuilt and redeployed
- Select the module you would like to build an image of by clicking the folder dropdown in the top right of the maven build wizard in IntelliJ (execute maven goal)
- `mvn clean install` will build a local docker image for the corresponding service
    - the default image tag is 'test' but can be easily configured in the root pom.xml file as <docker.tag>
    - the image will be built with the 'latest' and 'version' tags as well
- `mvn clean deploy` will build the docker image and deploy it to ECR (make sure you signed in to AWS CLI)
- You can also deploy to ECR by running `./deploy_to_docker.sh` or just pushing the single image using `docker push <image_name>:<tag>`

## Running Tests with IntelliJ and Maven:
- Using the built-in Maven tab (right-hand side by default), we can run the full scala test suite
  - Make sure the following Maven profiles are selected (and only those): nexus, scala, scala-test
  - Run the following docker-compose file: `docker-compose -f docker-compose-test.yml up -d`
  - Finally, run `mvn clean install` on the 'backend' folder

## Important Development Notes:
- Make sure project sdk/jdk is set to Java 17 inside IntelliJ
- Make sure Maven in IntelliJ is using the same project jdk (Java 17)
- Make sure you added framework support for scala 2.13 in IntelliJ for the mona project 
- When making a new branch for git, the naming convention is to follow YouTrack or GitHub Issue numbers (i.e. FIEHNLAB-3825)
- If you're having issues pulling packages with maven, you can troubleshoot by deleting your `~/.m2/repository` directory.
  Additionally, ensure you have the correct 'settings.xml' file in `~/.m2`. If problems still persist, try the 'Invalidate
  Caches' under the File tab in IntelliJ

## Deploying to Gose Repository:
- Make sure you have the following profiles selected: nexus, scala, scala-test
- Ensure you have the proper settings.xml file stored at `~/.m2` or selected in IntelliJ
- Run `mvn clean deploy` on each service as you would when building docker images
  - This will not deploy or create docker images, since the docker profile is not selected

### ports for cluster service nodes:
- persistence svr: 2222
- configuration svr: 1111
- discovery service: 8761
- auth server: 3333
- proxy service: 8080 (entry point)

### ports for in-memory node:
- postgresql db: 5432

<br/><br/>

<b>Note:</b> More documentation for team members can be found in the YouTrack Knowledge Base
