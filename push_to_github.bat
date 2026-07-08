@echo off
set GIT_PATH=D:\Program Files\Git\bin\git.exe
set REPO_URL=https://github.com/siciyuan/scroambase.git

echo Configuring git...
"%GIT_PATH%" config --global user.name "siciyuan"
"%GIT_PATH%" config --global user.email "siciyuan@users.noreply.github.com"
echo Git configured.

echo Adding files...
"%GIT_PATH%" add .
echo Files added.

echo Committing...
"%GIT_PATH%" commit -m "Initial commit: ScroamDB Economy Plugin with Payment and Treasury"
echo Committed.

echo Adding remote...
"%GIT_PATH%" remote add origin %REPO_URL%
echo Remote added.

echo Renaming branch...
"%GIT_PATH%" branch -M main
echo Branch renamed.

echo Pushing to GitHub...
"%GIT_PATH%" push -u origin main
echo Push completed.

pause