# Weekly Tasks Backend

Backend service for Tibia Weekly Tasks Recommendation system.

## Technologies

- Java 17
- Spring Boot 3.2.0
- MongoDB
- Maven

## Prerequisites

- Java 17 or higher
- MongoDB Atlas account (connection string configured)
- Maven 3.6+

## Configuration

### Option 1: Environment Variable (Recommended for Production)

Set the complete MongoDB URI:
```bash
# Windows PowerShell
$env:MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/?appName=weekly-tasks"

# Linux/Mac
export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/?appName=weekly-tasks"
```

### Option 2: Local Profile (Recommended for Development)

Create `application-local.yml` in `src/main/resources/`:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://username:password@cluster.mongodb.net/?appName=weekly-tasks
```

**Important:** This file is already in `.gitignore` and won't be committed.

Run with local profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Application Settings

Edit `src/main/resources/application.yml` to configure:
- MongoDB connection string (already configured for MongoDB Atlas)
- Server port (default: 8080)
- CORS allowed origins

## Running the Application

### Using Local Profile (Easiest for Development)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Environment Variable
```bash
# Set your MongoDB URI
$env:MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/?appName=weekly-tasks"

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

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
