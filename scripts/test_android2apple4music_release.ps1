param(
    [string]$JavaHome = "C:\Users\marc\jdk17-nospaces",
    [string]$AndroidHome = "C:\Users\marc\android-sdk-nospaces"
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

$env:JAVA_HOME = $JavaHome
$env:ANDROID_HOME = $AndroidHome
$env:ANDROID_SDK_ROOT = $AndroidHome

# Keep the test JVM command line stable. A malformed user PATH entry with a
# stray quote can break Gradle's forked test process through java.library.path.
$safePathParts = @(
    (Join-Path $JavaHome "bin"),
    (Join-Path $AndroidHome "platform-tools"),
    (Join-Path $AndroidHome "cmdline-tools\latest\bin"),
    "C:\Program Files\Git\usr\bin",
    "C:\Program Files\Git\cmd",
    "$env:SystemRoot\system32",
    "$env:SystemRoot",
    "$env:SystemRoot\System32\Wbem",
    "$env:SystemRoot\System32\WindowsPowerShell\v1.0"
)
$env:PATH = ($safePathParts | Where-Object { $_ -and (Test-Path -LiteralPath $_) }) -join ";"

Push-Location $repoRoot
try {
    & .\gradlew.bat :app:testReleaseUnitTest --rerun-tasks --no-daemon --console=plain
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle release unit tests failed"
    }
} finally {
    Pop-Location
}
