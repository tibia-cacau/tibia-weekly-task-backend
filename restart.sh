#!/bin/bash
# Restart Weekly Tasks Backend

echo "Restarting Weekly Tasks Backend..."
echo ""

# Stop the backend
./stop.sh

# Wait a moment
sleep 2

# Start the backend
./start.sh
