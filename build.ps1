<#
.SYNOPSIS
    Builds all GuruCool modules.
    Usage: .\build.ps1           (skip tests)
           .\build.ps1 -WithTests (run tests)
#>
param([switch]$WithTests)

$JAVA_HOME = "C:\Program Files\Java\jdk-21"
$MVN = "C:\Users\2485823\.m2\wrapper\dists\apache-maven-3.9.12-bin\5nmfsn99br87k5d4ajlekdq10k\apache-maven-3.9.12\bin\mvn.cmd"
$ROOT = "C:\Users\2485823\Downloads\GuruCool"

$env:JAVA_HOME = $JAVA_HOME

Write-Host "Building GuruCool..." -ForegroundColor Yellow
Set-Location $ROOT

if ($WithTests) {
    & $MVN clean install --no-transfer-progress
} else {
    & $MVN clean install -DskipTests --no-transfer-progress
}

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build SUCCESS" -ForegroundColor Green
} else {
    Write-Host "Build FAILED" -ForegroundColor Red
}
