# Weekly Tasks Backend - Local Development
# Run Spring Boot application with Maven

Write-Host "🚀 Starting Weekly Tasks Backend (Local Development)..." -ForegroundColor Green
Write-Host ""

# Navigate to backend directory
Set-Location $PSScriptRoot

# Load environment variables from .env file
if (Test-Path ".env") {
    Write-Host "📋 Loading environment variables from .env..." -ForegroundColor Cyan
    Get-Content ".env" | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            Set-Item -Path "env:$name" -Value $value
            Write-Host "  ✓ $name" -ForegroundColor Green
        }
    }
    Write-Host ""
}
else {
    Write-Host "⚠️  .env file not found! Copy .env.example to .env and configure your credentials." -ForegroundColor Yellow
    Write-Host ""
    exit 1
}

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "  - Port: $env:PORT"
Write-Host "  - Database: $($env:DATASOURCE_URL -replace 'jdbc:mysql://', '' -replace '\?.*', '')"
Write-Host "  - CORS: $env:CORS_ALLOWED_ORIGINS"
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Cyan
Write-Host ""

# Run Maven Spring Boot
mvn spring-boot:run
