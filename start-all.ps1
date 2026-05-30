<#
.SYNOPSIS
    Starts all GuruCool services individually (for distributed mode with real infra).
    For zero-dependency local dev, use start-local.ps1 instead.
#>

$JAVA_HOME = "C:\Program Files\Java\jdk-21"
$MVN = "C:\Users\2485823\.m2\wrapper\dists\apache-maven-3.9.12-bin\5nmfsn99br87k5d4ajlekdq10k\apache-maven-3.9.12\bin\mvn.cmd"
$ROOT = "C:\Users\2485823\Downloads\GuruCool"

$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$JAVA_HOME\bin;$env:PATH"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  GuruCool Backend — Starting Services  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

function Start-Service {
    param([string]$Name, [string]$Dir, [int]$Port)
    Write-Host "`nStarting $Name on port $Port..." -ForegroundColor Yellow
    $proc = Start-Process powershell -ArgumentList @(
        "-NoExit",
        "-Command",
        "`$env:JAVA_HOME='$JAVA_HOME'; `$env:PATH='$JAVA_HOME\bin;' + `$env:PATH; Set-Location '$ROOT\$Dir'; & '$MVN' spring-boot:run"
    ) -PassThru -WindowStyle Normal
    Write-Host "  PID: $($proc.Id)" -ForegroundColor Green
    Start-Sleep -Seconds 3
    return $proc.Id
}

$pids = @()

# Start in dependency order
$pids += Start-Service "Config Server"       "config-server"       8888
Write-Host "  Waiting for Config Server to be ready..." -ForegroundColor Gray
Start-Sleep -Seconds 10

$pids += Start-Service "Service Registry"    "service-registry"    8761
Start-Sleep -Seconds 8

$pids += Start-Service "User Service"        "user-service"        8081
$pids += Start-Service "Mentor Service"      "mentor-service"      8082
$pids += Start-Service "Session Service"     "session-service"     8083
$pids += Start-Service "Payment Service"     "payment-service"     8084
$pids += Start-Service "Notification Service" "notification-service" 8085
$pids += Start-Service "AI Service"          "ai-service"          8086
Start-Sleep -Seconds 15

$pids += Start-Service "API Gateway"         "api-gateway"         8080

# Save PIDs for stop script
$pids | Out-File -FilePath "$ROOT\.running-pids" -Encoding UTF8

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  All services started!" -ForegroundColor Green
Write-Host "  Swagger UI -> http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "  Eureka     -> http://localhost:8761" -ForegroundColor White
Write-Host "  Zipkin     -> http://localhost:9411" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Run .\stop-all.ps1 to stop all services" -ForegroundColor Gray
