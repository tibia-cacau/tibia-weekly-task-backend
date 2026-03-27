#!/bin/bash
# Stop Weekly Tasks Backend

echo "Stopping Weekly Tasks Backend..."

# Navigate to script directory
cd "$(dirname "$0")"

# Find all running instances
PIDS=$(ps aux | grep "[j]ava.*weekly-tasks-backend.*\.jar" | awk '{print $2}')

if [ -z "$PIDS" ]; then
    echo "⚠ Backend is not running"
    exit 0
fi

# Stop all instances
for PID in $PIDS; do
    echo "Stopping process $PID..."
    kill $PID
    
    # Wait up to 10 seconds for graceful shutdown
    for i in {1..10}; do
        if ! ps -p $PID > /dev/null 2>&1; then
            echo "✓ Process $PID stopped gracefully"
            break
        fi
        sleep 1
    done
    
    # Force kill if still running
    if ps -p $PID > /dev/null 2>&1; then
        echo "⚠ Force killing process $PID..."
        kill -9 $PID
    fi
done

echo "✓ Backend stopped successfully"
