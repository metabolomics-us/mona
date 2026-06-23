#!/usr/bin/env bash
# dev-deploy.sh — rebuild and redeploy one or more MoNA services locally
#
# Usage:
#   ./dev-deploy.sh <service> [service2 ...]
#
# Services:
#   persistence     backend/core/rest/persistence-server
#   auth            backend/core/rest/auth-server
#   proxy           backend/app/server/proxy
#   discovery       backend/core/discovery
#   config          backend/core/config
#   bootstrap       backend/core/bootstrap
#   webhooks        backend/services/webhooks-server
#   statistics      backend/services/statistics-server
#   similarity      backend/services/similarity
#   downloader      backend/services/download-scheduler
#   curation-runner backend/services/curation-runner
#   curation-scheduler backend/services/curation-scheduler
#
# Options:
#   --dry-run     Print what would be built and redeployed without running anything
#   --also-make   Rebuild upstream dependencies (slower, needed on cold .m2 cache)
#   --clean       Run clean before install (forces full recompile)
#
# Examples:
#   ./dev-deploy.sh persistence
#   ./dev-deploy.sh persistence auth
#   ./dev-deploy.sh --dry-run persistence auth

set -euo pipefail

# Scala 2.13.6 requires Java 17 — it cannot build its compiler bridge on Java 21+
export JAVA_HOME=/usr/lib/jvm/java-1.17.0-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BLUE='\033[0;34m'
BOLD='\033[1m'
DIM='\033[2m'
RESET='\033[0m'

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_ROOT="$REPO_ROOT/backend"
COMPOSE_FILE="$BACKEND_ROOT/docker-compose-dev.yml"
DOCKER_TAG="dev"
MVN_PROFILES="nexus,scala"

declare -A MODULE_PATHS=(
  [persistence]="core/rest/persistence-server"
  [auth]="core/rest/auth-server"
  [proxy]="app/server/proxy"
  [discovery]="core/discovery"
  [config]="core/config"
  [bootstrap]="core/bootstrap"
  [webhooks]="services/webhooks-server"
  [statistics]="services/statistics-server"
  [similarity]="services/similarity"
  [downloader]="services/download-scheduler"
  [curation-runner]="services/curation-runner"
  [curation-scheduler]="services/curation-scheduler"
)

# Docker image names: public.ecr.aws/fiehnlab/mona-{artifactId}:dev
declare -A IMAGE_NAMES=(
  [persistence]="public.ecr.aws/fiehnlab/mona-persistence-server"
  [auth]="public.ecr.aws/fiehnlab/mona-auth-server"
  [proxy]="public.ecr.aws/fiehnlab/mona-proxy"
  [discovery]="public.ecr.aws/fiehnlab/mona-discovery"
  [config]="public.ecr.aws/fiehnlab/mona-config"
  [bootstrap]="public.ecr.aws/fiehnlab/mona-bootstrap"
  [webhooks]="public.ecr.aws/fiehnlab/mona-webhooks-server"
  [statistics]="public.ecr.aws/fiehnlab/mona-statistics-server"
  [similarity]="public.ecr.aws/fiehnlab/mona-similarity"
  [downloader]="public.ecr.aws/fiehnlab/mona-download-scheduler"
  [curation-runner]="public.ecr.aws/fiehnlab/mona-curation-runner"
  [curation-scheduler]="public.ecr.aws/fiehnlab/mona-curation-scheduler"
)

# Compose service names (mostly match, but a few differ)
declare -A COMPOSE_NAMES=(
  [persistence]="persistence"
  [auth]="auth"
  [proxy]="proxy"
  [discovery]="discovery"
  [config]="config-server"
  [bootstrap]="bootstrap"
  [webhooks]="webhooks"
  [statistics]="statistics"
  [similarity]="similarity"
  [downloader]="downloader"
  [curation-runner]="curationRunner"
  [curation-scheduler]="curationScheduler"
)

# ─── Banner helpers ────────────────────────────────────────────────────────────

banner_width=60

print_separator() {
  local char="${1:-─}"
  printf "${DIM}"
  printf '%0.s'"$char" $(seq 1 $banner_width)
  printf "${RESET}\n"
}

print_banner() {
  local color="$1"
  local title="$2"
  local subtitle="${3:-}"
  echo ""
  print_separator "═"
  printf "${color}${BOLD}  %-$((banner_width - 4))s${RESET}\n" "$title"
  [[ -n "$subtitle" ]] && printf "${DIM}  %s${RESET}\n" "$subtitle"
  print_separator "═"
}

print_step() {
  local color="$1"
  local icon="$2"
  local msg="$3"
  printf "\n${color}${BOLD}  %s  %s${RESET}\n" "$icon" "$msg"
  print_separator
}

# ─── Argument parsing ──────────────────────────────────────────────────────────

DRY_RUN=false
ALSO_MAKE=false
CLEAN=false
SERVICES=()

for arg in "$@"; do
  if [[ "$arg" == "--dry-run" ]]; then
    DRY_RUN=true
  elif [[ "$arg" == "--also-make" || "$arg" == "-am" ]]; then
    ALSO_MAKE=true
  elif [[ "$arg" == "--clean" ]]; then
    CLEAN=true
  elif [[ "$arg" == "all" ]]; then
    SERVICES=("${!MODULE_PATHS[@]}")
  else
    SERVICES+=("$arg")
  fi
done

if [[ ${#SERVICES[@]} -eq 0 ]]; then
  echo "Usage: $0 [--dry-run] [--also-make] [--clean] <service> [service2 ...]"
  echo ""
  echo "  --also-make   Rebuild upstream dependencies (slower, needed on cold .m2 cache)"
  echo "  --clean       Run clean before install (forces full recompile)"
  echo ""
  echo "Available services:"
  for key in "${!MODULE_PATHS[@]}"; do
    printf "  %-20s %s\n" "$key" "${MODULE_PATHS[$key]}"
  done | sort
  exit 1
fi

# Validate all service names before doing anything
for svc in "${SERVICES[@]}"; do
  if [[ -z "${MODULE_PATHS[$svc]+_}" ]]; then
    echo "ERROR: Unknown service '$svc'"
    echo "Run $0 with no arguments to see available services."
    exit 1
  fi
done

# ─── Dry run ──────────────────────────────────────────────────────────────────

if [[ "$DRY_RUN" == true ]]; then
  print_banner "$CYAN" "DRY RUN — MoNA Dev Deploy" "No commands will be executed"
  echo ""
  printf "  ${DIM}flags: %s%s${RESET}\n" \
    "$([[ "$ALSO_MAKE" == true ]] && echo '--also-make ' || echo '')" \
    "$([[ "$CLEAN" == true ]] && echo '--clean' || echo 'incremental (no --clean)')"
  echo ""
  local_idx=0
  total=${#SERVICES[@]}
  for svc in "${SERVICES[@]}"; do
    (( local_idx++ )) || true
    printf "  ${BOLD}[%d/%d]${RESET} ${CYAN}%s${RESET}\n" "$local_idx" "$total" "$svc"
    printf "        module:  %s\n" "${MODULE_PATHS[$svc]}"
    printf "        image:   %s:%s\n" "${IMAGE_NAMES[$svc]}" "$DOCKER_TAG"
    printf "        compose: %s\n" "${COMPOSE_NAMES[$svc]}"
    echo ""
  done
  exit 0
fi

# ─── ECR Login ────────────────────────────────────────────────────────────────

print_banner "$BLUE" "MoNA Dev Deploy" "Authenticating with AWS ECR..."
echo ""
printf "  ${CYAN}Logging in to public.ecr.aws/fiehnlab...${RESET}\n"
if aws ecr-public get-login-password --region us-east-1 \
    | docker login --username AWS --password-stdin public.ecr.aws/fiehnlab; then
  printf "  ${GREEN}ECR login successful${RESET}\n"
else
  printf "  ${YELLOW}WARNING: ECR login failed — image pulls may not work${RESET}\n"
fi

# ─── Build & Deploy ───────────────────────────────────────────────────────────

total=${#SERVICES[@]}
idx=0
overall_start=$SECONDS

declare -a BUILT=()
declare -a DEPLOYED=()
declare -a FAILED=()
declare -a SKIPPED=()
declare -A BUILD_DURATIONS=()

format_duration() {
  local secs=$1
  if (( secs < 60 )); then
    printf "%ds" "$secs"
  else
    printf "%dm %ds" $(( secs / 60 )) $(( secs % 60 ))
  fi
}

for svc in "${SERVICES[@]}"; do
  (( idx++ )) || true
  module="${MODULE_PATHS[$svc]}"
  image="${IMAGE_NAMES[$svc]}:$DOCKER_TAG"
  compose_svc="${COMPOSE_NAMES[$svc]}"

  print_banner "$CYAN" "[$idx/$total]  Building: $svc" "$module"

  # Snapshot image timestamp before build
  before=$(docker inspect --format '{{.Created}}' "$image" 2>/dev/null || echo "none")

  MVN_GOALS="install"
  [[ "$CLEAN" == true ]] && MVN_GOALS="clean install"
  MVN_AM_FLAG=""
  [[ "$ALSO_MAKE" == true ]] && MVN_AM_FLAG="-am"

  build_start=$SECONDS
  build_ok=true
  # shellcheck disable=SC2086
  mvn $MVN_GOALS \
    -P "$MVN_PROFILES" \
    -DskipTests \
    -Ddocker.tag="$DOCKER_TAG" \
    -pl "$module" \
    $MVN_AM_FLAG \
    -f "$BACKEND_ROOT/pom.xml" || build_ok=false
  build_dur=$(( SECONDS - build_start ))
  BUILD_DURATIONS["$svc"]=$build_dur

  if [[ "$build_ok" == false ]]; then
    print_step "$RED" "✗" "BUILD FAILED: $svc  ($(format_duration $build_dur))"
    FAILED+=("$svc (build)")
    continue
  fi

  print_step "$GREEN" "✓" "BUILD COMPLETE: $svc  [$idx/$total]  $(format_duration $build_dur)"
  BUILT+=("$svc")

  # Verify image was actually updated
  after=$(docker inspect --format '{{.Created}}' "$image" 2>/dev/null || echo "none")

  if [[ "$after" == "none" ]]; then
    printf "\n  ${YELLOW}WARNING: Image %s not found — skipping redeploy${RESET}\n" "$image"
    SKIPPED+=("$svc (no image)")
    continue
  fi

  if [[ "$before" == "$after" ]]; then
    printf "\n  ${YELLOW}WARNING: Image timestamp unchanged — build may not have produced a new image${RESET}\n"
    printf "  ${DIM}before: %s${RESET}\n" "$before"
    read -rp "  Redeploy anyway? [Y/n] " confirm
    if [[ "$confirm" =~ ^[Nn]$ ]]; then
      printf "  ${DIM}Skipping redeploy of %s${RESET}\n" "$svc"
      SKIPPED+=("$svc (image unchanged)")
      continue
    fi
  else
    printf "\n  ${DIM}Image updated:${RESET}\n"
    printf "  ${DIM}  before: %s${RESET}\n" "$before"
    printf "  ${DIM}  after:  %s${RESET}\n" "$after"
  fi

  print_step "$BLUE" "↑" "REDEPLOYING: $compose_svc"
  deploy_ok=true
  docker compose -f "$COMPOSE_FILE" up -d --no-deps "$compose_svc" || deploy_ok=false

  if [[ "$deploy_ok" == false ]]; then
    printf "  ${RED}${BOLD}✗  Redeploy failed for %s${RESET}\n" "$svc"
    FAILED+=("$svc (deploy)")
  else
    printf "  ${GREEN}${BOLD}✓  %s is up${RESET}\n" "$compose_svc"
    DEPLOYED+=("$svc")
  fi
done

# ─── Summary ──────────────────────────────────────────────────────────────────

total_dur=$(( SECONDS - overall_start ))
print_banner "$BOLD" "Summary" "$(date '+%Y-%m-%d %H:%M:%S')  —  total $(format_duration $total_dur)"
echo ""

if [[ ${#BUILT[@]} -gt 0 ]]; then
  printf "  ${GREEN}${BOLD}Built & Imaged   (${#BUILT[@]})${RESET}\n"
  for s in "${BUILT[@]}"; do
    printf "    ${GREEN}✓${RESET}  %-24s ${DIM}%s${RESET}\n" "$s" "$(format_duration "${BUILD_DURATIONS[$s]}")"
  done
  echo ""
fi

if [[ ${#DEPLOYED[@]} -gt 0 ]]; then
  printf "  ${BLUE}${BOLD}Redeployed       (${#DEPLOYED[@]})${RESET}\n"
  for s in "${DEPLOYED[@]}"; do printf "    ${BLUE}↑${RESET}  %s\n" "$s"; done
  echo ""
fi

if [[ ${#SKIPPED[@]} -gt 0 ]]; then
  printf "  ${YELLOW}${BOLD}Skipped          (${#SKIPPED[@]})${RESET}\n"
  for s in "${SKIPPED[@]}"; do printf "    ${YELLOW}-${RESET}  %s\n" "$s"; done
  echo ""
fi

if [[ ${#FAILED[@]} -gt 0 ]]; then
  printf "  ${RED}${BOLD}Failed           (${#FAILED[@]})${RESET}\n"
  for s in "${FAILED[@]}"; do printf "    ${RED}✗${RESET}  %s\n" "$s"; done
  echo ""
fi

print_separator "═"

if [[ ${#FAILED[@]} -gt 0 ]]; then
  printf "\n  ${RED}${BOLD}Completed with errors.${RESET}\n\n"
  exit 1
else
  printf "\n  ${GREEN}${BOLD}All done.${RESET}\n\n"
fi
