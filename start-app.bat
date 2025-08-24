@echo off
echo Starting the Spring Boot application...
echo Make sure you have Java 17+ installed and available in your PATH
echo.

REM Try to run with Maven first
echo Attempting to run with Maven...
mvn clean compile spring-boot:run
if %ERRORLEVEL% EQU 0 (
    echo Application started successfully with Maven
    exit /b 0
)

echo.
echo Maven approach failed, trying with Maven wrapper...
.\mvnw clean compile spring-boot:run
if %ERRORLEVEL% EQU 0 (
    echo Application started successfully with Maven wrapper
    exit /b 0
)

echo.
echo Both Maven approaches failed. Please check your environment setup.
echo Make sure you have Java 17+ and Maven properly installed.
pause