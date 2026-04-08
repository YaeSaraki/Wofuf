#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

WITH_INFRA=false
MODE="monolith"

usage() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  --mode monolith      Use monolithic mode (single backend) [default]"
    echo "  --mode distributed   Use distributed mode (microservices)"
    echo "  --infra              Include infrastructure services (MySQL, Redis, Kafka, MinIO)"
    echo "  --build              Build images before starting"
    echo "  --force              Force recreate containers"
    echo "  -h, --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                              # Start monolith app only"
    echo "  $0 --mode monolith --infra      # Start monolith + infrastructure"
    echo "  $0 --mode distributed --infra   # Start all microservices + infrastructure"
}

BUILD_FLAG=""
FORCE_FLAG=""
COMPOSE_PROFILES=""

parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --mode)
                MODE="$2"
                shift 2
                ;;
            --infra)
                WITH_INFRA=true
                shift
                ;;
            --build)
                BUILD_FLAG="--build"
                shift
                ;;
            --force)
                FORCE_FLAG="--force-recreate"
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            *)
                echo -e "${RED}Unknown option: $1${NC}"
                usage
                exit 1
                ;;
        esac
    done
}

# Main
parse_args "$@"

# Check if .env.production exists
if [ ! -f "$PROJECT_ROOT/.env.production" ]; then
    echo -e "${YELLOW}Warning: .env.production not found. Copying from .env.production.example${NC}"
    cp "$PROJECT_ROOT/.env.production.example" "$PROJECT_ROOT/.env.production"
    echo -e "${YELLOW}Please review and update .env.production before deploying!${NC}"
fi

cd "$PROJECT_ROOT"

if [ "$MODE" = "distributed" ]; then
    COMPOSE_FILE="docker-compose.dist.yml"
else
    COMPOSE_FILE="docker-compose.prod.yml"
fi

if [ "$WITH_INFRA" = true ]; then
    COMPOSE_PROFILES="--profile infra"
fi

if [ "$MODE" = "distributed" ]; then
    echo -e "${GREEN}Starting distributed stack...${NC}"
else
    if [ "$WITH_INFRA" = true ]; then
        echo -e "${GREEN}Starting monolith + infrastructure...${NC}"
    else
        echo -e "${GREEN}Starting monolith (external infrastructure)...${NC}"
    fi
fi

docker compose -f "$COMPOSE_FILE" $COMPOSE_PROFILES up -d $BUILD_FLAG $FORCE_FLAG

echo ""
echo -e "${GREEN}Services started!${NC}"
echo ""

if [ "$MODE" = "distributed" ]; then
    echo "Frontend:     http://localhost:${FRONTEND_PORT:-80}"
    echo "Gateway:      http://localhost:${GATEWAY_PORT:-9999}"
    echo "Discovery:    http://localhost:${DISCOVERY_PORT:-8761}"
    echo "Users:        http://localhost:${USERS_PORT:-8001}"
    echo "Players:      http://localhost:${PLAYERS_PORT:-8002}"
    echo "Forum:        http://localhost:${FORUM_PORT:-8003}"
else
    echo "Frontend:  http://localhost:${FRONTEND_PORT:-80}"
    echo "Backend:   http://localhost:${BACKEND_PORT:-8005}"
fi

if [ "$WITH_INFRA" = true ]; then
    echo "MySQL:     localhost:${MYSQL_EXTERNAL_PORT:-3307}"
    echo "Redis:     localhost:${REDIS_EXTERNAL_PORT:-6380}"
    echo "Kafka:     localhost:${KAFKA_EXTERNAL_PORT:-9092}"
    echo "MinIO:     localhost:${MINIO_EXTERNAL_PORT:-9000} (Console: ${MINIO_EXTERNAL_CONSOLE_PORT:-9001})"
fi

echo ""
echo "View logs: docker compose -f $COMPOSE_FILE logs -f"
