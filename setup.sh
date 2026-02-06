#!/bin/bash

# Money Manager Backend - Setup Script
# This script helps set up the Money Manager Backend application

set -e

echo "=========================================="
echo "Money Manager Backend - Setup Script"
echo "=========================================="
echo ""

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 17 or higher."
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "\K[^"]*')
echo "✓ Java version: $JAVA_VERSION"
echo "✓ Maven is installed"
echo ""

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "Creating .env file from template..."
    cp .env.example .env
    echo "✓ .env file created. Please review and update if needed."
    echo ""
fi

# Create logs directory
if [ ! -d logs ]; then
    echo "Creating logs directory..."
    mkdir -p logs
    echo "✓ logs directory created"
fi

# Build the application
echo "Building the application..."
mvn clean install

echo ""
echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "To start the application, run:"
echo "  mvn spring-boot:run"
echo ""
echo "Or use:"
echo "  java -jar target/money-manager-backend-1.0.0.jar"
echo ""
echo "The application will be available at:"
echo "  http://localhost:8080"
echo ""
echo "API Documentation:"
echo "  http://localhost:8080/api-docs (Swagger)"
echo ""
