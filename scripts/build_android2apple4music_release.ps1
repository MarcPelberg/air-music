param(
    [string]$JavaHome = "C:\Users\marc\Documents\New project 4\.tools\jdk17-extract2\jdk-17.0.19+10",
    [string]$AndroidHome = "C:\Users\marc\Documents\New project 4\.tools\android-sdk",
    [string]$OutputApk = ""
)

$ErrorActionPreference = "Stop"

function New-Secret([int]$bytes) {
    $buffer = New-Object byte[] $bytes
    $rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
    try {
        $rng.GetBytes($buffer)
    } finally {
        $rng.Dispose()
    }
    return [Convert]::ToBase64String($buffer).Replace("+", "A").Replace("/", "B").Replace("=", "C")
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$keyDir = Join-Path $env:USERPROFILE ".android"
$keyPath = Join-Path $keyDir "android2apple4music-release.p12"
$localPropsPath = Join-Path $repoRoot "local.properties"

New-Item -ItemType Directory -Force -Path $keyDir | Out-Null

if (Test-Path -LiteralPath $localPropsPath) {
    $props = @{}
    Get-Content -LiteralPath $localPropsPath | ForEach-Object {
        if ($_ -match "^([^=]+)=(.*)$") {
            $props[$matches[1]] = $matches[2]
        }
    }
    $storePassword = $props["storePassword"]
    if ($props["storeFile"]) {
        $keyPath = $props["storeFile"].Replace("/", "\")
    }
} else {
    $storePassword = New-Secret 30
}

if (!(Test-Path -LiteralPath $localPropsPath) -and !(Test-Path -LiteralPath $keyPath)) {
    $env:ANDROID2APPLE4MUSIC_KEYSTORE_PASSWORD = $storePassword
    & "$JavaHome\bin\keytool.exe" `
        -genkeypair `
        -v `
        -keystore $keyPath `
        -storetype PKCS12 `
        -storepass:env ANDROID2APPLE4MUSIC_KEYSTORE_PASSWORD `
        -alias android2apple4music `
        -keyalg RSA `
        -keysize 4096 `
        -validity 10000 `
        -dname "CN=Marc Android2Apple4Music, OU=Personal, O=Android2Apple4Music, L=Local, ST=Local, C=US"
    if ($LASTEXITCODE -ne 0) {
        throw "keytool failed"
    }
}

if (!(Test-Path -LiteralPath $localPropsPath)) {
    $storeFile = $keyPath.Replace("\", "/")
    $content = @(
        "storeFile=$storeFile",
        "storePassword=$storePassword",
        "keyAlias=android2apple4music",
        "keyPassword=$storePassword"
    ) -join "`n"
    [System.IO.File]::WriteAllText($localPropsPath, $content + "`n")
}

$env:JAVA_HOME = $JavaHome
$env:ANDROID_HOME = $AndroidHome
$env:ANDROID_SDK_ROOT = $AndroidHome
$env:PATH = "$JavaHome\bin;$AndroidHome\platform-tools;$AndroidHome\cmdline-tools\latest\bin;C:\Program Files\Git\usr\bin;" + $env:PATH

Push-Location $repoRoot
try {
    $gradleArgs = @("assembleRelease", "--no-daemon", "--console=plain")
    & .\gradlew.bat @gradleArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle release build failed"
    }

    $aapt2 = Join-Path $AndroidHome "build-tools\35.0.0\aapt2.exe"
    python scripts\check_android2apple4music_apk.py --aapt2 $aapt2 --apk app\build\outputs\apk\release\app-release.apk
    if ($LASTEXITCODE -ne 0) {
        throw "Android2Apple4Music APK check failed"
    }

    if ($OutputApk) {
        $outputDir = Split-Path -Parent $OutputApk
        if ($outputDir) {
            New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
        }
        Copy-Item -LiteralPath "app\build\outputs\apk\release\app-release.apk" -Destination $OutputApk -Force
        Write-Host "Copied APK to $OutputApk"
    }
} finally {
    Pop-Location
}
