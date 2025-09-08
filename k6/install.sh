#!/bin/bash

# K6 Load Testing Setup Script for Warehouse Operations
set -e

echo "🚀 Setting up K6 Load Testing Environment"
echo "==========================================="

# Check if we're in the k6 directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: This script should be run from the k6 directory"
    echo "Please run: cd k6 && ./install.sh"
    exit 1
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check for required tools
print_status "Checking for required tools..."

# Check for Node.js and nvm
if command -v nvm &> /dev/null; then
    print_success "nvm is available"
    
    # Use the Node version specified in .nvmrc
    if [ -f ".nvmrc" ]; then
        print_status "Using Node.js version specified in .nvmrc"
        nvm use
        if [ $? -ne 0 ]; then
            print_warning "Node.js version not installed, installing..."
            nvm install
            nvm use
        fi
    else
        print_warning ".nvmrc file not found, using default Node.js version"
    fi
elif command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    print_success "Node.js is available: $NODE_VERSION"
else
    print_error "Node.js is not available. Please install Node.js or nvm"
    echo "Visit: https://nodejs.org/ or https://github.com/nvm-sh/nvm"
    exit 1
fi

# Check for curl
if ! command -v curl &> /dev/null; then
    print_error "curl is required but not installed"
    exit 1
fi

# Create necessary directories
print_status "Creating directory structure..."
mkdir -p bin reports data

# Detect OS and architecture
OS="$(uname -s)"
ARCH="$(uname -m)"

print_status "Detected OS: $OS, Architecture: $ARCH"

# Set K6 download URL based on OS and architecture
K6_VERSION="v0.47.0"
case "$OS" in
    Linux*)
        case "$ARCH" in
            x86_64) K6_URL="https://github.com/grafana/k6/releases/download/${K6_VERSION}/k6-${K6_VERSION}-linux-amd64.tar.gz";;
            aarch64|arm64) K6_URL="https://github.com/grafana/k6/releases/download/${K6_VERSION}/k6-${K6_VERSION}-linux-arm64.tar.gz";;
            *) print_error "Unsupported Linux architecture: $ARCH"; exit 1;;
        esac
        ;;
    Darwin*)
        case "$ARCH" in
            x86_64) K6_URL="https://github.com/grafana/k6/releases/download/${K6_VERSION}/k6-${K6_VERSION}-macos-amd64.tar.gz";;
            arm64) K6_URL="https://github.com/grafana/k6/releases/download/${K6_VERSION}/k6-${K6_VERSION}-macos-arm64.tar.gz";;
            *) print_error "Unsupported macOS architecture: $ARCH"; exit 1;;
        esac
        ;;
    MINGW*|CYGWIN*|MSYS*)
        case "$ARCH" in
            x86_64) K6_URL="https://github.com/grafana/k6/releases/download/${K6_VERSION}/k6-${K6_VERSION}-windows-amd64.zip";;
            *) print_error "Unsupported Windows architecture: $ARCH"; exit 1;;
        esac
        ;;
    *)
        print_error "Unsupported operating system: $OS"
        exit 1
        ;;
esac

# Download and install K6
print_status "Downloading K6 ${K6_VERSION}..."
if [ ! -f "bin/k6" ]; then
    cd bin
    
    if [[ "$K6_URL" == *.zip ]]; then
        # Windows ZIP file
        curl -L "$K6_URL" -o k6.zip
        unzip -j k6.zip "*/k6.exe"
        rm k6.zip
        mv k6.exe k6
    else
        # Unix TAR.GZ file
        curl -L "$K6_URL" | tar xvz --strip-components=1
    fi
    
    cd ..
    
    if [ -f "bin/k6" ]; then
        chmod +x bin/k6
        print_success "K6 installed successfully"
    else
        print_error "Failed to install K6"
        exit 1
    fi
else
    print_success "K6 already installed"
fi

# Verify K6 installation
print_status "Verifying K6 installation..."
K6_VERSION_OUTPUT=$(./bin/k6 version)
if [ $? -eq 0 ]; then
    print_success "K6 verification passed: $K6_VERSION_OUTPUT"
else
    print_error "K6 verification failed"
    exit 1
fi

# Install Node.js dependencies (if any)
if [ -f "package.json" ]; then
    print_status "Installing Node.js dependencies..."
    npm install
    print_success "Node.js dependencies installed"
fi

# Create sample environment files if they don't exist
if [ ! -f "config/local.env" ]; then
    print_status "Creating sample environment configuration..."
    cat > config/local.env << EOF
# Local Environment Configuration for K6 Tests
ENVIRONMENT=local
BASE_URL=http://localhost:8080
MONGODB_URI=mongodb://localhost:27017/warehouse
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Test Configuration
TEST_DURATION=5m
VIRTUAL_USERS=10
RAMP_UP_TIME=1m
EOF
    print_success "Created config/local.env"
fi

# Make scripts executable
print_status "Setting script permissions..."
find scripts -name "*.js" -exec chmod +r {} \;
chmod +x install.sh 2>/dev/null || true

# Create PATH update script
print_status "Creating PATH helper script..."
cat > setup-path.sh << 'EOF'
#!/bin/bash
# Add K6 to PATH for current session
export PATH="$(pwd)/bin:$PATH"
echo "✅ K6 added to PATH for this session"
echo "🚀 You can now run: k6 run scripts/smoke-test.js"
EOF
chmod +x setup-path.sh

print_success "K6 Load Testing Environment Setup Complete!"
echo ""
echo "📋 Quick Start Guide:"
echo "====================="
echo ""
echo "1. 🔧 Prepare your environment:"
echo "   source setup-path.sh              # Add k6 to PATH"
echo "   # OR add $(pwd)/bin to your PATH permanently"
echo ""
echo "2. 🏃 Run tests:"
echo "   k6 run scripts/smoke-test.js       # Basic functionality test"
echo "   k6 run scripts/load-test.js        # Normal load test"
echo "   k6 run scripts/stress-test.js      # Stress test"
echo "   k6 run scripts/spike-test.js       # Spike test"
echo ""
echo "3. 🎯 Focused tests:"
echo "   k6 run scripts/picklist-load-test.js  # Pick list operations"
echo "   k6 run scripts/package-load-test.js   # Package operations"
echo ""
echo "4. 🌍 Environment configuration:"
echo "   ENVIRONMENT=staging k6 run scripts/load-test.js"
echo "   ENVIRONMENT=local k6 run scripts/smoke-test.js"
echo ""
echo "5. 📊 With custom parameters:"
echo "   k6 run --vus 20 --duration 10m scripts/load-test.js"
echo ""
echo "📁 Directory Structure:"
echo "   scripts/     - Test scripts"
echo "   config/      - Environment configurations"  
echo "   data/        - Test data files"
echo "   reports/     - Test result reports"
echo "   bin/         - K6 binary"
echo ""
echo "🔍 Before running tests, ensure:"
echo "   ✅ Warehouse Operations service is running"
echo "   ✅ MongoDB is accessible"
echo "   ✅ Kafka is running (for integration tests)"
echo ""
echo "🚀 Happy Load Testing!"