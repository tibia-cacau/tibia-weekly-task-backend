#!/bin/bash
# Start Weekly Tasks Backend

echo "Starting Weekly Tasks Backend..."

# Navigate to script directory
cd "$(dirname "$0")"

# Load environment variables from .env file (optional)
if [ -f .env ]; then
    echo "Loading environment variables from .env..."
    set -a  # Automatically export all variables
    source .env
    set +a  # Stop auto-exporting
fi

# Find the JAR file (use the most recent one)
JAR_FILE=$(ls -t weekly-tasks-backend-*.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "✗ Error: No JAR file found!"
    echo "  Expected: weekly-tasks-backend-*.jar"
    exit 1
fi

echo "Using JAR: $JAR_FILE"

# Check if already running
if ps aux | grep "[j]ava.*weekly-tasks-backend.*\.jar" > /dev/null; then
    echo "⚠ Backend is already running!"
    echo "  Use ./stop.sh to stop it first, or ./restart.sh to restart"
    exit 1
fi

# Start the backend
nohup java -jar "$JAR_FILE" > backend.log 2>&1 &
PID=$!

# Wait a moment and check if it started
sleep 2

if ps -p $PID > /dev/null; then
    echo "✓ Backend started successfully!"
    echo "  PID: $PID"
    echo "  Log: backend.log"
    echo ""
    echo "To check status: tail -f backend.log"
    echo "To stop: ./stop.sh"
else
    echo "✗ Failed to start backend"
    echo "Check backend.log for errors"
    exit 1
fi
