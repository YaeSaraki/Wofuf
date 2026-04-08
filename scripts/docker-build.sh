#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

usage() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  --mode monolith      Build monolithic backend + frontend [default]"
    echo "  --mode distributed   Build all 5 microservice modules + frontend"
    echo "  --backend            Build backend image only (monolith)"
    echo "  --frontend           Build frontend image only"
    echo "  --all                Build both backend and frontend (monolith)"
    echo "  -t, --tag            Tag for images (default: latest)"
    echo "  --no-cache           Build without cache"
    echo "  -h, --help           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --mode monolith              # Build monolith (same as --all)"
    echo "  $0 --mode distributed           # Build all distributed images"
    echo "  $0 --backend -t v1.0            # Build monolith backend with tag"
}

IMAGE_TAG="latest"
BUILD_BACKEND=false
BUILD_FRONTEND=false
DOCKER_CACHE=""
MODE="monolith"

parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --mode)
                MODE="$2"
                shift 2
                ;;
            --backend)
                BUILD_BACKEND=true
                shift
                ;;
            --frontend)
                BUILD_FRONTEND=true
                shift
                ;;
            --all)
                BUILD_BACKEND=true
                BUILD_FRONTEND=true
                shift
                ;;
            -t|--tag)
                IMAGE_TAG="$2"
                shift 2
                ;;
            --no-cache)
                DOCKER_CACHE="--no-cache"
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

build_backend() {
    echo -e "${YELLOW}Building monolith backend image: wofuf-backend:$IMAGE_TAG${NC}"
    cd "$PROJECT_ROOT"
    docker build $DOCKER_CACHE -t "wofuf-backend:$IMAGE_TAG" -f Dockerfile .
    echo -e "${GREEN}Monolith backend image build complete!${NC}"
}

build_frontend() {
    local tag_suffix=""
    if [ "$MODE" = "distributed" ]; then
        tag_suffix="-dist"
    fi
    echo -e "${YELLOW}Building frontend image: wofuf-frontend${tag_suffix}:$IMAGE_TAG${NC}"
    cd "$PROJECT_ROOT/WofuF"
    local dockerfile="Dockerfile"
    if [ "$MODE" = "distributed" ]; then
        dockerfile="Dockerfile.dist"
    fi
    docker build $DOCKER_CACHE -t "wofuf-frontend${tag_suffix}:$IMAGE_TAG" -f "$dockerfile" .
    echo -e "${GREEN}Frontend image build complete!${NC}"
}

build_distributed() {
    echo -e "${YELLOW}=== Building Distributed Mode Images ===${NC}"

    # Module definitions: GRADLE_MODULE_NAME|DOCKER_DIR|PORT
    local modules=(
        "infra-discovery|Wofuf-infra/Wofuf-discovery|8761"
        "infra-gateway|Wofuf-infra/Wofuf-gateway|9999"
        "modules-users|Wofuf-modules/Wofuf-users|8001"
        "modules-players|Wofuf-modules/Wofuf-players|8002"
        "modules-forum|Wofuf-modules/Wofuf-forum|8003"
    )

    local total=$((${#modules[@]} + 1))
    local count=0

    for module_def in "${modules[@]}"; do
        count=$((count + 1))
        IFS='|' read -r gradle_name project_dir port <<< "$module_def"
        local image_name="wofuf-${gradle_name#*-}"  # infra-discovery -> discovery
        echo -e "${YELLOW}[$count/$total] Building $gradle_name -> $image_name:$IMAGE_TAG${NC}"
        cd "$PROJECT_ROOT"
        docker build $DOCKER_CACHE \
            -t "$image_name:$IMAGE_TAG" \
            -f Dockerfile.module \
            --build-arg "MODULE=$gradle_name" \
            --build-arg "PROJECT_DIR=$project_dir" \
            --build-arg "PORT=$port" \
            .
        echo -e "${GREEN}  $image_name:$IMAGE_TAG done${NC}"
    done

    # Build frontend (distributed variant)
    count=$((count + 1))
    build_frontend_dist "$count" "$total"
}

build_frontend_dist() {
    local count=$1
    local total=$2
    echo -e "${YELLOW}[$count/$total] Building frontend (distributed): wofuf-frontend-dist:$IMAGE_TAG${NC}"
    cd "$PROJECT_ROOT/WofuF"
    docker build $DOCKER_CACHE -t "wofuf-frontend-dist:$IMAGE_TAG" -f Dockerfile.dist .
    echo -e "${GREEN}  wofuf-frontend-dist:$IMAGE_TAG done${NC}"
}

# Main
parse_args "$@"

if [ "$MODE" = "distributed" ]; then
    build_distributed
else
    # Default to building all for monolith
    if [ "$BUILD_BACKEND" = false ] && [ "$BUILD_FRONTEND" = false ]; then
        BUILD_BACKEND=true
        BUILD_FRONTEND=true
    fi

    if [ "$BUILD_BACKEND" = true ]; then
        build_backend
    fi

    if [ "$BUILD_FRONTEND" = true ]; then
        build_frontend
    fi
fi

echo ""
echo -e "${GREEN}All Docker images built successfully!${NC}"
echo ""
echo "Built images:"
docker images "wofuf-*:$IMAGE_TAG" --format "  {{.Repository}}:{{.Tag}}  ({{.Size}})"
