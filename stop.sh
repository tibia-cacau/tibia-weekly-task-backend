#!/bin/bash

# Stop script for KingHost deployment

if [ -f application.pid ]; then
    PID=$(cat application.pid)
    echo "Stopping application with PID: $PID"
    kill $PID
    rm application.pid
    echo "Application stopped"
else
    echo "No PID file found. Searching for running process..."
    pkill -f "weekly-tasks-backend-1.0.0.jar"
    echo "Process killed (if any)"
fi
