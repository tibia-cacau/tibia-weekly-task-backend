#!/bin/bash
# Check Weekly Tasks Backend Status

echo "Weekly Tasks Backend - Status Check"
echo "===================================="
echo ""

# Check if process is running
if pgrep -f "java.*weekly-tasks-backend.*\.jar" > /dev/null; then
    PID=$(pgrep -f "java.*weekly-tasks-backend.*\.jar")
    echo "Status: ✓ RUNNING"
    echo "PID: $PID"
    
    # Show process details
    ps -p $PID -o pid,ppid,cmd,%mem,%cpu,etime
    
    echo ""
    echo "Memory usage:"
    ps -p $PID -o rss= | awk '{printf "  %.2f MB\n", $1/1024}'
    
else
    echo "Status: ✗ STOPPED"
fi

echo ""
echo "Recent logs (last 10 lines):"
echo "----------------------------"
if [ -f backend.log ]; then
    tail -n 10 backend.log
else
    echo "No log file found (backend.log)"
fi
