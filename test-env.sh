#!/bin/bash
# Test if .env is being loaded correctly

echo "Testing .env loading..."

cd "$(dirname "$0")"

if [ ! -f .env ]; then
    echo "✗ .env file not found!"
    exit 1
fi

echo "Loading .env..."
set -a
source .env
set +a

echo ""
echo "Environment variables loaded:"
echo "  DATASOURCE_URL: $DATASOURCE_URL"
echo "  DATASOURCE_USERNAME: $DATASOURCE_USERNAME"
echo "  DATASOURCE_PASSWORD: [${#DATASOURCE_PASSWORD} chars]"
echo "  PORT: $PORT"
echo ""

if [ -z "$DATASOURCE_URL" ]; then
    echo "✗ DATASOURCE_URL is empty! Check .env format"
    exit 1
fi

if [ -z "$DATASOURCE_PASSWORD" ]; then
    echo "✗ DATASOURCE_PASSWORD is empty! Check .env format"
    exit 1
fi

echo "✓ All variables loaded successfully!"
