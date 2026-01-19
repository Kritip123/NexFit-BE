#!/bin/bash

echo "🚀 TrainerHub Backend - Quick Start"
echo "===================================="
echo ""

# Check if Maven wrapper exists
if [ ! -f "./mvnw" ]; then
    echo "❌ Maven wrapper not found. Please run from project root."
    exit 1
fi

# Make Maven wrapper executable
chmod +x ./mvnw

echo "📦 Building application (skipping tests for faster build)..."
./mvnw clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo ""
echo "✅ Build successful!"
echo ""
echo "📝 Next steps:"
echo ""
echo "1. Start with Docker Compose (includes all databases):"
echo "   docker-compose up -d"
echo ""
echo "2. Or run locally (requires databases to be installed):"
echo "   ./mvnw spring-boot:run"
echo ""
echo "3. Access Swagger UI:"
echo "   http://localhost:8080/api/v1/swagger-ui.html"
echo ""
echo "4. Check health endpoint:"
echo "   curl http://localhost:8080/api/v1/actuator/health"
echo ""
echo "For more details, see BUILD-INSTRUCTIONS.md"
