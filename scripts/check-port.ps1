param(
  [int]$Port = 8080,
  [string]$Host = '127.0.0.1'
)

$listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $listener) {
  Write-Host "Port $Port is free."
  exit 0
}

$process = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
if ($process) {
  Write-Host "Port $Port is already in use by PID $($process.Id) ($($process.ProcessName))."
} else {
  Write-Host "Port $Port is already in use by PID $($listener.OwningProcess)."
}

exit 1
