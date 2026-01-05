#!/bin/bash

# KingHost Java Application Startup Script
# Weekly Tasks Backend - Spring Boot 3.2.0 with MySQL

# Set environment variables for KingHost MySQL database
export DATASOURCE_URL="jdbc:mysql://mysql50-farm1.kinghost.net:3306/tibiacacau?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DATASOURCE_USERNAME="tibiacacau"
export DATASOURCE_PASSWORD="tibiacacau123"

# Set server port (default 8080)
export PORT=8080

# Set CORS allowed origins (production domains)
export CORS_ALLOWED_ORIGINS="http://tibiacacau.com.br,https://tibiacacau.com.br,https://tibia-cacau.github.io"

# Java options
export JAVA_OPTS="-Xms256m -Xmx512m"

# Start the application
nohup java $JAVA_OPTS -jar weekly-tasks-backend-1.0.0.jar > application.log 2>&1 &

# Save PID
echo $! > application.pid

echo "Application started with PID: $(cat application.pid)"
echo "Log file: application.log"
echo ""
echo "To stop: kill \$(cat application.pid)"
echo "To view logs: tail -f application.log"
