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
    echo "  --monolith        Build backend monolith (root project) [default]"
    echo "  --module <name>   Build a specific module (shared, shared-auth, modules-users, etc.)"
    echo "  --all-modules     Build all modules individually"
    echo "  --frontend        Build frontend only"
    echo "  --all             Build everything (monolith + frontend)"
    echo "  --skip-tests      Skip running tests"
    echo "  -h, --help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --monolith              # Build backend monolith"
    echo "  $0 --module modules-players # Build players module only"
    echo "  $0 --frontend              # Build frontend"
    echo "  $0 --all                   # Build everything"
}

BUILD_BACKEND=false
BUILD_FRONTEND=false
BUILD_ALL_MODULES=false
MODULE_NAME=""
SKIP_TESTS=""

parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --monolith)
                BUILD_BACKEND=true
                shift
                ;;
            --module)
                BUILD_BACKEND=true
                MODULE_NAME="$2"
                if [ -z "$MODULE_NAME" ]; then
                    echo -e "${RED}Error: --module requires a module name${NC}"
                    exit 1
                fi
                shift 2
                ;;
            --all-modules)
                BUILD_ALL_MODULES=true
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
            --skip-tests)
                SKIP_TESTS="-x test"
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

build_backend_monolith() {
    echo -e "${YELLOW}[1/1] Building backend monolith...${NC}"
    cd "$PROJECT_ROOT"
    ./gradlew bootJar $SKIP_TESTS --no-daemon
    echo -e "${GREEN}Backend monolith build complete!${NC}"
}

build_module() {
    local module="$1"
    echo -e "${YELLOW}Building module: $module${NC}"
    cd "$PROJECT_ROOT"
    ./gradlew ":$module:build" $SKIP_TESTS --no-daemon
    echo -e "${GREEN}Module $module build complete!${NC}"
}

build_all_modules() {
    local modules=("shared" "shared-auth" "modules-users" "modules-players" "modules-forum" "infra-discovery" "infra-gateway")
    local total=${#modules[@]}
    local count=0
    for module in "${modules[@]}"; do
        count=$((count + 1))
        echo -e "${YELLOW}[$count/$total] Building module: $module${NC}"
        cd "$PROJECT_ROOT"
        ./gradlew ":$module:build" $SKIP_TESTS --no-daemon
    done
    echo -e "${GREEN}All $total modules build complete!${NC}"
}

build_frontend() {
    echo -e "${YELLOW}Building frontend...${NC}"
    cd "$PROJECT_ROOT/WofuF"
    if [ ! -d "node_modules" ]; then
        echo -e "${YELLOW}Installing frontend dependencies...${NC}"
        npm ci
    fi
    npm run build
    echo -e "${GREEN}Frontend build complete!${NC}"
}

# Main
parse_args "$@"

# Default to monolith if no option specified
if [ "$BUILD_BACKEND" = false ] && [ "$BUILD_FRONTEND" = false ] && [ "$BUILD_ALL_MODULES" = false ]; then
    BUILD_BACKEND=true
fi

if [ "$BUILD_ALL_MODULES" = true ]; then
    build_all_modules
fi

if [ "$BUILD_BACKEND" = true ]; then
    if [ -n "$MODULE_NAME" ]; then
        build_module "$MODULE_NAME"
    else
        build_backend_monolith
    fi
fi

if [ "$BUILD_FRONTEND" = true ]; then
    build_frontend
fi

echo -e "${GREEN}Build finished successfully!${NC}"
