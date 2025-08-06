# MassBank of North America Application

## Common:
- Highly Recommended to use Linux OS (Preferably Ubuntu LTS 18.04 or 20.04)
- Ensure you have the following installed
  - Node Version Manager(NVM)
  - Docker Engine (And Docker-Compose)
  - NPM (Node Package Manager)
  - Angular CLI 10.2.3
  - IntelliJ IDEA Ultimate
  - Maven 3 (Should be baked into IntelliJ)
  - Git
  - AWS CLI (for ECR to pull docker images)
  - Java 1.8_292 (open_jdk x64)
  - Scala 2.13
    - Installs in IntelliJ
    - Right-click the main 'mona' project and click 'Add Framework Support'
- Gain Access to the following
  - Internal mona-config private repo
    - You'll need admin access and will need to generate a Personal Access Token (https://github.com/settings/tokens)
    - Then open your `~/.bash_rc` and set 'GIT_USER' and 'GIT_PASS' to your GitHub username and
      new personal access token. This allows the discovery service to work as intended
    - Note: You'll need to set these tokens on the IPA and GOSE servers as they expire every year (you can configure
      how quickly these expire).
  - Access to AWS account and the ability to push to ECR
  - Admin access to public mona repo
  - You'll need a settings.xml file for your maven 'User Settings File' this will include the nexus profile
    and all the necessary repo's for pulling down libraries. Please ask a developer on the team for this config
    and store the file at `~/.m2`


## Starting Development:
- There are 3 scripts in the project to easily start development
  - `./start_corsproxy.sh`
    - Solves CORS issues when connecting the frontend to the microservices
  - `./start_docker_dev.sh`
    - Starts all necessary docker microservices
  - `./start_frontend.sh`
    - Starts the Angular 10 frontend with ng serve to develop on the fly

## PROD Deployment:
- Production deployment is simple with docker
  - Bring down the application with: `docker stack rm mona`
  - Bring the application back up with `docker stack deploy -c docker-compose-prod.yml`
  - Note: You may need to force the newest images to pull down with `docker-compose -f docker-compose-prod.yml pull`

## Running Test Suite With IntelliJ and Maven:
- Using the built-in Maven tab, we can run the full scala test suite
  - Make sure the following Maven profiles are selected (and only those): nexus, scala, scala-test
  - Run the following docker-compose file: `docker-compose -f docker-compose-test.yml -d`
  - Finally, run `mvn clean install` on the 'backend(root)'

## Generate new Docker Images:
- Make sure the following Maven profiles are selected (and only those): nexus, scala, docker
- The following services can be built into docker images: discovery, bootstrap, webhooks-server, curation-scheduler,
  repository, persistence-server, auth-server, similarity, proxy, download-scheduler, and curation-runner
- `mvn clean install` will build a local docker image for the corresponding service
- `mvn clean deploy` will build the docker image and deploy it to ECR (make sure you signed in to AWS CLI)
  - default tag is 'test' you can easily configure that in the main mona pom.xml
- NOTE: For the proxy service pom.xml, you'll need to configure the 'ng.env' variable to 'staging' or 'prod' 
  depending on if the build is for IPA('staging') or GOSE('prod');

## Important Development Notes:
- Make sure project sdk/jdk is set to Java 1.8_292
- Make sure to configure Maven in IntelliJ IDEA to use the same project jdk which should be Java 1.8_292
- Make sure you added framework support for scala 2.13 in IntelliJ for the mona project
- When making a new branch for git, naming convention should typically have a ticket number (i.e. FIEHN-1234) following
  a slash and a description of the ticket (i.e. FIEHN-1234/ThisIsATicket).
- If you're having issues pulling packages with maven, you can troubleshoot with deleting your `~/.m2/repository` directory.
  Additionally, ensure you have the correct 'settings.xml' file in `~/.m2`. If problems still persist, try the 'Invalidate
  Caches' under the File tab in IntelliJ IDEA.


### ports for cluster service nodes:
- persistence svr: 2222
- configuration svr: 1111
- discovery service: 8761
- auth server: 3333
- proxy service: 8080 (entry point)

### ports for in-memory node:
- postgresql db : 5432
