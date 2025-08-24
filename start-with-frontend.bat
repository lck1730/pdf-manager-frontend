@echo off
echo Starting MinIO and backend services...
start "MinIO & Backend" /D "%~dp0" start-app.bat

echo Waiting for backend to start...
timeout /t 10 /nobreak >nul

echo Starting frontend development server...
cd frontend
npm run dev