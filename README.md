# MassBank of North America (MoNA)

The MoNA application can be found at https://mona.fiehnlab.ucdavis.edu/ or https://massbank.us/

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Starting Development](#starting-development)
3. [Architecture & Services](#architecture--services)
4. [Running Tests](#running-tests)
5. [Building Docker Images](#building-docker-images)
6. [Production Deployment](#production-deployment)
7. [Deploying to Gose Repository](#deploying-to-gose-repository)
8. [Development Notes](#development-notes)

---

## Getting Started

It is highly recommended to use Linux for development (preferably Ubuntu LTS 18.04 or later).

### Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java (JDK) | 17 | Set as project SDK in IntelliJ |
| Scala | 2.13 | Install via IntelliJ plugin |
| Maven | 3.x | Should come with IntelliJ; requires custom `settings.xml` |
| IntelliJ IDEA | Ultimate | Required for Scala support |
| Angular CLI | 10.2.3 | — |
| NVM | — | Node Version Manager |
| NPM | — | Node Package Manager |
| Docker & Docker Compose | — | — |
| AWS CLI | — | Required for ECR image push/pull |
| corsproxy | — | `npm install -g corsproxy` (npm v8) |

### Access Requirements

**Private `mona-config` repo**

You'll need a GitHub Personal Access Token with access to the repo (https://github.com/settings/tokens). Then open your `~/.bashrc` and set `GIT_USER` and `GIT_PASS` to your GitHub username and personal access token — this allows the discovery service to work as intended. Tokens expire yearly and will need to be reconfigured each time.

**AWS credentials**

You'll need an AWS account with permissions to push and pull from ECR. Store credentials under `~/.aws/`.

**Maven `settings.xml`**

You'll need a `settings.xml` for your Maven User Settings File — this includes the Nexus profile and all necessary repos for pulling down libraries. Ask a developer on the team for this config and store it under `~/.m2/`.

---

## Starting Development

There are three scripts in the root of the project to easily start development:

**1. Start all necessary Docker microservices:**
```bash
./start_docker_dev.sh
```

**2. Solve CORS issues between the frontend and microservices:**
```bash
./start_corsproxy.sh
```

**3. Start the Angular frontend with live reload at `localhost:9090`:**
```bash
./start_frontend.sh
```

### AWS CloudWatch logging

To use the `awslogs` driver in the Docker Compose files, configure your Docker installation with AWS credentials that have CloudWatch permissions.

For Docker installed with apt:
```bash
sudo systemctl edit docker
```

For Docker installed with snap:
```bash
sudo systemctl edit snap.docker.dockerd.service
```

Then add the credentials:
```ini
[Service]
Environment="AWS_ACCESS_KEY_ID=<id>"
Environment="AWS_SECRET_ACCESS_KEY=<secret>"
```

---

## Architecture & Services

MoNA is a microservices application. All services run as Docker containers.

| Service | ECR image | Port | Notes |
|---|---|---|---|
| proxy | mona-proxy | 8080 | Entry point; also serves the frontend build |
| discovery | mona-discovery | 8761 | Eureka service registry |
| config-server | mona-config | 1111 | Spring Cloud Config; reads from GitHub |
| webhooks | mona-webhooks-server | 4444 | — |
| curationScheduler | mona-curation-scheduler | 5555 | — |
| similarity | mona-similarity | 9999 | — |
| downloader | mona-download-scheduler | 7777 | — |
| postgresql | postgres:13.4 | 5432 | Primary database |
| rabbitmq | rabbitmq:3.10-management | 15672 / 5672 | Management UI / AMQP |
| bootstrap | mona-bootstrap | internal | Data initialization |
| persistence | mona-persistence-server | internal | Core data persistence |
| auth | mona-auth-server | internal | Authentication & token issuance |
| statistics | mona-statistics-server | internal | — |
| curationRunner | mona-curation-runner | internal | — |

> **Frontend changes** only require rebuilding and redeploying the `proxy` image.

> In production, an `nginx` service handles SSL termination on ports 80/443. In dev, `postgresql-hero` (PgHero) is available on port 8081.

---

## Backups

The `generate_backup.sh` script is used to generate backups of the postgres database on Gose. It is run monthly with cron. See the script for more details.

---

## Running Tests

**Maven profiles required:** `nexus`, `scala`, `scala-test`

1. Start the test environment:
   ```bash
   docker compose -f docker-compose-test.yml up -d
   ```

2. Using the built-in Maven tab in IntelliJ (right-hand side by default), run `mvn clean install` on the `backend` folder.
- Or run `mvn clean test` from the terminal inside the `backend/` folder.

---

## Building Docker Images

**Maven profiles required:** `nexus`, `scala`, `docker`

The following services can be built into Docker images: `discovery`, `bootstrap`, `webhooks-server`, `curation-scheduler`, `repository`, `persistence-server`, `auth-server`, `similarity`, `proxy`, `download-scheduler`, `curation-runner`, and `statistics-server`.

Select the module to build using the folder dropdown in IntelliJ's Maven build wizard.

| Command | What it does |
|---|---|
| `mvn clean install` | Builds a local Docker image tagged `test` (configurable via `<docker.tag>` in root `pom.xml`); also builds with `latest` and version tags |
| `./deploy_to_docker.sh` | Pushes the production images to ECR |
| `docker push <image_name>:<tag>` | Pushes a single image to ECR |

Ensure you have logged in to the AWS CLI before pushing to ECR.

---

## Production Deployment

MoNA's production instance is hosted on the Gose server using Docker Compose. Ask a team member for access to Gose.

Ensure your AWS credentials and GitHub PAT are set up on Gose before proceeding (details on YouTrack).

```bash
# Find where the docker compose file is located
docker compose ls

# Navigate to that directory, then pull the newest images from ECR
docker compose -f docker-compose-prod.yml pull

# Bring down the application
docker compose -f docker-compose-prod.yml down

# Bring the application back up
docker compose -f docker-compose-prod.yml up -d
```

> Pulling new images does not affect the current deployment — changes only take effect after `up -d`.

---

## Deploying to Gose Repository

This deploys compiled JARs to the internal Nexus/Maven repository

**Maven profiles required:** `nexus`, `scala`, `scala-test`

Ensure you have the proper `settings.xml` stored at `~/.m2/` or selected in IntelliJ, then run `mvn clean deploy` on each service.

---

## Development Notes

- Make sure the project SDK/JDK is set to Java 17 inside IntelliJ
- Make sure Maven in IntelliJ is using the same project JDK (Java 17)
- Make sure you have added framework support for Scala 2.13 in IntelliJ for the MoNA project
- Branch naming convention: follow YouTrack or GitHub issue numbers (e.g. `FIEHNLAB-3825`)
- If you're having issues pulling packages with Maven, try deleting your `~/.m2/repository/` directory. Ensure you have the correct `settings.xml` in `~/.m2/`. If problems persist, try **File → Invalidate Caches** in IntelliJ.
- The JWT token used for curation expires every 10 years. It can be found in the private `mona-config` repo in `application-prod.yml`. The current one expires May 23rd 2036. Curation will throw a 401 error if the token expires. It is planned to refactor the implementation so that manually refreshing the token is no longer required. If it was not refactored, generate a new token by following the steps below:
```
  # Step 1: Login to get a short-lived token
  TOKEN=$(curl -s -X POST https://mona.fiehnlab.ucdavis.edu/rest/auth/login \
    -H 'Content-Type: application/json' \
    -d '{"emailAddress":"<admin-email>","password":"<admin-password>"}' | python3 -c "import sys,json;
  print(json.load(sys.stdin)['token'])")

  # Step 2: Extend it to a 10-year token (this will print another token, this is the one we need)
  curl -s -X POST https://mona.fiehnlab.ucdavis.edu/rest/auth/extend \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $TOKEN" \
    -d "{\"token\":\"$TOKEN\"}"

  # Step 3: Replace the token in the config file and restart the curationRunner container
```

---

More documentation for team members can be found in the YouTrack Knowledge Base.
