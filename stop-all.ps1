<#
.SYNOPSIS
    Stops all GuruCool services started by start-all.ps1
#>

$ROOT = "C:\Users\2485823\Downloads\GuruCool"
$pidsFile = "$ROOT\.running-pids"

Write-Host "Stopping GuruCool services..." -ForegroundColor Yellow

# Kill by port
$ports = @(8080, 8081, 8082, 8083, 8084, 8085, 8086, 8761, 8888)
foreach ($port in $ports) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        $proc = Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host "  Stopping port $port (PID $($proc.Id): $($proc.ProcessName))" -ForegroundColor Gray
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        }
    }
}

# Clean up pids file
if (Test-Path $pidsFile) { Remove-Item $pidsFile -Force }

Write-Host "All services stopped." -ForegroundColor Green
