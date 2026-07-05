param(
    [int]$Port = 8080,
    [int]$StartupTimeoutSeconds = 30
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

try {
    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($listener) {
        $pid = $listener.OwningProcess
        $process = Get-Process -Id $pid -ErrorAction SilentlyContinue
        if ($process) {
            $cmdLine = (Get-CimInstance Win32_Process -Filter "ProcessId = $pid" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty CommandLine -ErrorAction SilentlyContinue)
            if ($cmdLine -and $cmdLine -match 'DevBrain|spring-boot:run|java') {
                Write-Host "Reusing running application on port $Port (PID $pid)."
                exit 0
            }
            Write-Host "Port $Port is already occupied by another process (PID $pid). Startup aborted."
            exit 1
        }
    }

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = Join-Path $projectRoot 'mvnw.cmd'
    $startInfo.WorkingDirectory = $projectRoot
    $startInfo.Arguments = 'spring-boot:run'
    $startInfo.RedirectStandardOutput = $false
    $startInfo.RedirectStandardError = $false
    $startInfo.UseShellExecute = $false

    $process = [System.Diagnostics.Process]::Start($startInfo)
    $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Seconds 1
        $probe = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($probe) {
            Write-Host "Application started successfully on port $Port."
            exit 0
        }
        if ($process.HasExited) {
            Write-Host "Spring Boot exited before becoming ready on port $Port."
            exit 1
        }
    }

    Write-Host "Startup could not continue within $StartupTimeoutSeconds seconds because port $Port remained blocked."
    if (-not $process.HasExited) {
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
    exit 1
}
catch {
    Write-Host "Startup blocked: $($_.Exception.Message)"
    exit 1
}
