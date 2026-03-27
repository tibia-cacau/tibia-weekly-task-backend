# Weekly Tasks Backend

Backend service for Tibia Weekly Tasks Recommendation system.

## Technologies

- Java 17
- Spring Boot 3.2.0
- MySQL 8
- Maven

## Prerequisites

- Java 17 or higher
- MySQL 8+ (local or remote)
- Maven 3.6+

## ⚙️ Configuration

### 🔒 Environment Variables (Required)

**All credentials are stored in environment variables for security.**

1. **Copy the example file:**

    ```bash
    cp .env.example .env
    ```

2. **Edit `.env` with your real credentials:**

    ```env
    DATASOURCE_URL=jdbc:mysql://your-host:3306/your-database?useSSL=false&serverTimezone=UTC
    DATASOURCE_USERNAME=your-username
    DATASOURCE_PASSWORD=your-password
    PORT=8080
    CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:4201
    REIDOSCOINS_API_URL=https://www.reidoscoins.com.br/index.php
    REIDOSCOINS_API_TOKEN=your-api-token
    ```

3. **⚠️ NEVER commit the `.env` file!** (already in `.gitignore`)

## 🚀 Running the Application

### Development - Windows (PowerShell)

```powershell
.\start-local.ps1
```

### Development - Linux/Mac

```bash
chmod +x start-dev.sh  # First time only
./start-dev.sh
```

Both scripts will:

- ✓ Load environment variables from `.env`
- ✓ Start Spring Boot with Maven (hot reload enabled)
- ✓ Show configuration details

The API will be available at `http://localhost:8080`

### Manual Start

```bash
# Load .env variables manually, then:
mvn spring-boot:run
```

---

## 📦 Building for Production

### Option 1: Automated Build (Linux)

```bash
chmod +x build-and-deploy.sh  # First time only
./build-and-deploy.sh
```

This will:

1. Clean previous builds
2. Compile the JAR (skip tests)
3. Ask if you want to start it immediately

### Option 2: Manual Build

```bash
mvn clean package -DskipTests
```

The JAR will be in `target/weekly-tasks-backend-*.jar`

---

## 🖥️ Running in Production (Linux Server)

### Step 1: Upload Files to Server

```bash
# On your local machine
scp -r weekly-tasks-backend/ user@your-server:/path/to/app/

# Or use Git
ssh user@your-server
cd /path/to/app
git clone https://github.com/your-repo/weekly-tasks-backend
cd weekly-tasks-backend
```

### Step 2: Configure Environment

```bash
# Create .env from template
cp .env.example .env
nano .env  # or vim, vi, etc.

# Fill with your production credentials:
# DATASOURCE_URL=jdbc:mysql://your-prod-db:3306/dbname...
# DATASOURCE_USERNAME=prod_user
# DATASOURCE_PASSWORD=secure_password
# PORT=8080
# CORS_ALLOWED_ORIGINS=https://your-domain.com
```

### Step 3: Build the JAR

```bash
# Make scripts executable (first time only)
chmod +x *.sh

# Build
./build-and-deploy.sh
# Or manually:
mvn clean package -DskipTests
```

### Step 4: Start the Application

```bash
# Start in background
./start.sh

# Output:
# 🚀 Starting Weekly Tasks Backend (Production)...
# 📋 Loading environment variables from .env...
#    ✓ Environment variables loaded
# 📦 Using JAR: target/weekly-tasks-backend-1.0.0.jar
# ✅ Application started successfully!
#    PID: 12345
```

### Step 5: Monitor & Manage

```bash
# View real-time logs
tail -f application.log

# Check if running
ps -p $(cat application.pid)

# Stop the application
./stop.sh

# Restart
./stop.sh && ./start.sh
```

---

## 🔄 Deploy Updates

When you update the code:

```bash
# Pull latest code
git pull origin main

# Rebuild and restart
./stop.sh
./build-and-deploy.sh
# Answer 'y' to start automatically
```

---

## 🌐 Deploy to Cloud Platforms

### Render.com / Railway / Heroku

Just set the environment variables in your hosting platform:

- `DATASOURCE_URL`
- `DATASOURCE_USERNAME`
- `DATASOURCE_PASSWORD`
- `PORT`
- `CORS_ALLOWED_ORIGINS`
- `REIDOSCOINS_API_TOKEN`

## API Endpoints

### Weekly Tasks

- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get task by ID
- `GET /api/tasks/type/{taskType}` - Get tasks by type (ITEM_DELIVERY or MONSTER_KILL)
- `GET /api/tasks/difficulty/{maxDifficulty}` - Get tasks by max difficulty
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

### Recommendations

- `POST /api/recommendations` - Get task recommendations based on player criteria

## Models

### WeeklyTask

- taskType: ITEM_DELIVERY or MONSTER_KILL
- name: Task name
- itemName/itemQuantity: For item delivery tasks
- monsterName/killCount: For monster kill tasks
- location: Where to complete the task
- difficulty: 1-5 scale
- estimatedTime: Minutes to complete
- rewardType/rewardAmount: Reward information
- requirements: List of requirements (level, quest, etc.)
- notes: Additional notes

### RecommendationRequest

- playerLevel: Player's current level
- vocation: Player's vocation (optional)
- maxDifficulty: Maximum difficulty level (optional)
- maxTimeMinutes: Maximum time in minutes (optional)

## Development

The application uses:

- Spring Data MongoDB for database operations
- Bean Validation for request validation
- Lombok for reducing boilerplate code
- SLF4J for logging
