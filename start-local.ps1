<#
.SYNOPSIS
    Starts GuruCool ALL-IN-ONE (no Docker, no PostgreSQL, no Redis, no Kafka needed).
    Uses embedded Kafka, H2 database, in-memory cache, mock payments, mock AI.
.DESCRIPTION
    Single JVM, single port (8080), everything included.
    Perfect for demos, interviews, and local development.
#>

$JAVA_HOME = "C:\Program Files\Java\jdk-21"
$MVN = "C:\Users\2485823\.m2\wrapper\dists\apache-maven-3.9.12-bin\5nmfsn99br87k5d4ajlekdq10k\apache-maven-3.9.12\bin\mvn.cmd"
$ROOT = "C:\Users\2485823\Downloads\GuruCool"

$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$JAVA_HOME\bin;$env:PATH"

Write-Host ""
Write-Host "  ____                    ____            _   " -ForegroundColor Cyan
Write-Host " / ___|_   _ _ __ _   _ / ___|___   ___ | |  " -ForegroundColor Cyan
Write-Host "| |  _| | | | '__| | | | |   / _ \ / _ \| |  " -ForegroundColor Cyan
Write-Host "| |_| | |_| | |  | |_| | |__| (_) | (_) | |  " -ForegroundColor Cyan
Write-Host " \____|\__,_|_|   \__,_|\____\___/ \___/|_|  " -ForegroundColor Cyan
Write-Host ""
Write-Host "  All-in-One Local Dev Launcher" -ForegroundColor Green
Write-Host "  No Docker  | No PostgreSQL  | No Redis  | No Kafka" -ForegroundColor Green
Write-Host ""

# Build first
Write-Host "Building project..." -ForegroundColor Yellow
Set-Location $ROOT
& $MVN clean install -DskipTests --no-transfer-progress -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed! Check errors above." -ForegroundColor Red
    exit 1
}
Write-Host "Build successful!" -ForegroundColor Green

# Run the all-in-one launcher
Write-Host ""
Write-Host "Starting GuruCool All-in-One on port 8080..." -ForegroundColor Yellow
Write-Host "   Swagger UI -> http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "   H2 Console -> http://localhost:8080/h2-console" -ForegroundColor White
Write-Host "   Health     -> http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "   Press Ctrl+C to stop." -ForegroundColor Gray
Write-Host ""

Set-Location "$ROOT\app-launcher"
& $MVN spring-boot:run -Dspring-boot.run.profiles=local
