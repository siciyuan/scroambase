@echo off
chcp 65001 >nul
setlocal

set "GIT=D:\Program Files\Git\bin\git.exe"
set "DIR=C:\Users\scroam\Desktop\scroambase"
set "REPO=https://github.com/siciyuan/scroambase.git"

cd /d "%DIR%"

echo ==================== Git Status ====================
"%GIT%" status
echo.

echo ==================== Git Add ====================
"%GIT%" add .
echo.

echo ==================== Git Commit ====================
"%GIT%" commit -m "Initial commit: ScroamDB Economy Plugin"
echo.

echo ==================== Git Remote ====================
"%GIT%" remote add origin %REPO%
echo.

echo ==================== Git Branch ====================
"%GIT%" branch -M main
echo.

echo ==================== Git Push ====================
"%GIT%" push -u origin main
echo.

echo ==================== Done ====================
pause