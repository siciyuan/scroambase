@echo off
chcp 65001 >nul
setlocal

set "GIT=D:\Program Files\Git\bin\git.exe"
set "DIR=C:\Users\scroam\Desktop\scroambase"

cd /d "%DIR%"

echo === Git Add ===
"%GIT%" add .

echo === Git Commit ===
"%GIT%" commit -m "Add login system with registration, authentication and session management"

echo === Git Push ===
"%GIT%" push -u origin main

echo === Done ===
pause