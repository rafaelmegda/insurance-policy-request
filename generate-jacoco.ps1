param(
  [switch]$Open = $true
)

$ErrorActionPreference = "Stop"

Write-Host "Running mvn clean test jacoco:report ..."
$mvn = "mvn"
& $mvn test jacoco:report

$reportPath = Join-Path -Path (Get-Location) -ChildPath "target\site\jacoco\index.html"

if (Test-Path $reportPath) {
    Write-Host "JaCoCo report generated at $reportPath"
    if ($Open) {
        Write-Host "Opening report..."
        Start-Process $reportPath
    } else {
        Write-Host "To open: $reportPath"
    }
} else {
    Write-Error "JaCoCo report not found at $reportPath"
    exit 1
}
