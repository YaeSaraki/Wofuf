#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

MODE="monolith"
REMOVE_VOLUMES=false

usage() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  --mode monolith      Stop monolithic stack [default]"
    echo "  --mode distributed   Stop distributed stack"
    echo "  --volumes            Remove data volumes (WARNING: deletes all data)"
    echo "  -h, --help           Show this help message"
}

COMPOSE_PROFILES=""

parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --mode)
                MODE="$2"
                shift 2
                ;;
            --volumes)
                REMOVE_VOLUMES=true
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

cd "$PROJECT_ROOT"

if [ "$MODE" = "distributed" ]; then
    COMPOSE_FILE="docker-compose.dist.yml"
else
    COMPOSE_FILE="docker-compose.prod.yml"
fi

if [ "$REMOVE_VOLUMES" = true ]; then
    echo -e "${RED}WARNING: This will remove all data volumes for $MODE mode!${NC}"
    read -p "Are you sure? (y/N): " confirm
    if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
        echo "Cancelled."
        exit 0
    fi
    docker compose -f "$COMPOSE_FILE" --profile infra down -v
    echo -e "${GREEN}All $MODE services and volumes removed.${NC}"
else
    docker compose -f "$COMPOSE_FILE" --profile infra down
    echo -e "${GREEN}All $MODE services stopped.${NC}"
fi
