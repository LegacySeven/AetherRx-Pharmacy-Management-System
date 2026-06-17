# AetherRx Environment Diagnostics Script
# Checks Java, JavaFX availability, and Maven configurations.

Clear-Host
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "         AETHERRX JAVA & JAVAFX ENVIRONMENT DIAGNOSTICS" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""

$JavaInstalled = $false
$MavenInstalled = $false
$HasJavaFX = $false

# 1. Test Java Command
Write-Host "[*] Checking Java status..." -NoNewline
try {
    $javaVer = java -version 2>&1 | Out-String
    if ($LASTEXITCODE -eq 0 -or $javaVer -match "version") {
        Write-Host " SUCCESS" -ForegroundColor Green
        Write-Host "    Found Java Version Details:" -ForegroundColor Gray
        $javaLines = java -version 2>&1
        foreach ($l in $javaLines) {
            Write-Host "    > $l" -ForegroundColor Gray
        }
        $JavaInstalled = $true
    } else {
        Write-Host " FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host " FAILED (Not found in PATH)" -ForegroundColor Red
}

# 2. Test Maven Command
Write-Host ""
Write-Host "[*] Checking Maven status..." -NoNewline
try {
    $mvnVer = mvn -v 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host " SUCCESS" -ForegroundColor Green
        Write-Host "    Found Maven version: $($mvnVer[0])" -ForegroundColor Gray
        $MavenInstalled = $true
    } else {
        Write-Host " FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host " FAILED (Not found in PATH)" -ForegroundColor Red
}

# 3. Check for built-in JavaFX support
Write-Host ""
Write-Host "[*] Inspecting JavaFX bundle status..." -NoNewline
if ($JavaInstalled) {
    # Attempt to load a JavaFX class dynamically via a quick java evaluation
    $testCode = 'public class TestFX { public static void main(String[] args) { try { Class.forName("javafx.application.Application"); System.exit(0); } catch(Exception e) { System.exit(1); } } }'
    $testCode | Out-File -FilePath "$PSScriptRoot\TestFX.java" -Encoding ascii
    
    try {
        & javac "$PSScriptRoot\TestFX.java" 2>$null
        if ($LASTEXITCODE -eq 0) {
            & java -cp $PSScriptRoot TestFX 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Host " BUILT-IN DETECTED!" -ForegroundColor Green
                Write-Host "    Excellent! Your JDK includes JavaFX out of the box. You don't need any complex CLI configuration." -ForegroundColor Gray
                $HasJavaFX = $true
            } else {
                Write-Host " NOT DETECTED" -ForegroundColor Yellow
                Write-Host "    Your Java compiler works, but the runtime does not include JavaFX." -ForegroundColor Gray
            }
        } else {
            Write-Host " NOT DETECTED" -ForegroundColor Yellow
            Write-Host "    JavaFX classes are not on the compiler path by default." -ForegroundColor Gray
        }
    } catch {
        Write-Host " SKIPPED (Compiler error)" -ForegroundColor Yellow
    } finally {
        # Clean up temporary test files
        if (Test-Path "$PSScriptRoot\TestFX.java") { Remove-Item "$PSScriptRoot\TestFX.java" -Force }
        if (Test-Path "$PSScriptRoot\TestFX.class") { Remove-Item "$PSScriptRoot\TestFX.class" -Force }
    }
} else {
    Write-Host " SKIPPED (Requires Java first)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================= SUMMARY =========================================" -ForegroundColor Cyan
if ($JavaInstalled -and $MavenInstalled) {
    Write-Host " [+] All base dependencies found!" -ForegroundColor Green
    if ($HasJavaFX) {
        Write-Host " [+] Complete standard setup ready! You can run:" -ForegroundColor Green
        Write-Host "     > mvn clean javafx:run" -ForegroundColor Cyan
    } else {
        Write-Host " [i] Setup is close! You have JDK and Maven, but need JavaFX modules." -ForegroundColor Yellow
        Write-Host "     Maven will automatically download JavaFX when you build the project!" -ForegroundColor LightGreen
        Write-Host "     Simply run: " -NoNewline
        Write-Host "mvn clean javafx:run" -ForegroundColor Cyan
    }
} else {
    Write-Host " [!] Action Required: Some development tools are missing." -ForegroundColor Red
    if (-not $JavaInstalled) {
        Write-Host "     - Please download and install BellSoft Liberica Full JDK or Zulu FX JDK." -ForegroundColor Yellow
    }
    if (-not $MavenInstalled) {
        Write-Host "     - If you don't want to install Maven globally, don't worry!" -ForegroundColor Yellow
        Write-Host "       The project will pull down dependencies automatically inside your IDE." -ForegroundColor Yellow
    }
}
Write-Host "===========================================================================================" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to exit..."
