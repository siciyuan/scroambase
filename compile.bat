@echo off
chcp 65001 >nul
setlocal

set "JAVA_HOME=C:\Users\scroam\Downloads\MSL\Java\21"
set "JAVAC=%JAVA_HOME%\bin\javac.exe"
set "JAR=%JAVA_HOME%\bin\jar.exe"
set "DIR=C:\Users\scroam\Desktop\scroambase"
set "SRC=%DIR%\src\main\java"
set "OUT=%DIR%\target\classes"
set "CP=%DIR%\target\classes"

if not exist "%OUT%" mkdir "%OUT%"

set "LIBS="
for %%f in (
    "%DIR%\lib\paper-api-1.21.11-R0.1-SNAPSHOT.jar"
    "%DIR%\lib\adventure-api-4.26.1.jar"
    "%DIR%\lib\adventure-key-4.26.1.jar"
    "%DIR%\lib\adventure-text-minimessage-4.26.1.jar"
    "%DIR%\lib\adventure-text-serializer-legacy-4.26.1.jar"
    "%DIR%\lib\adventure-text-serializer-gson-4.26.1.jar"
    "%DIR%\lib\bungeecord-chat-1.21-R0.2-deprecated+build.21.jar"
    "%DIR%\lib\gson-2.13.2.jar"
    "%DIR%\lib\sqlite-jdbc-3.49.1.0.jar"
) do (
    if defined LIBS (
        set "LIBS=%LIBS%;%%f"
    ) else (
        set "LIBS=%%f"
    )
)

echo === Compiling ===
"%JAVAC%" -d "%OUT%" -cp "%LIBS%" "%SRC%\com\scroam\db\data\PlayerData.java" "%SRC%\com\scroam\db\data\Waypoint.java" "%SRC%\com\scroam\db\data\TransactionRecord.java" "%SRC%\com\scroam\db\manager\DatabaseManager.java" "%SRC%\com\scroam\db\manager\TreasuryManager.java" "%SRC%\com\scroam\db\manager\EconomyManager.java" "%SRC%\com\scroam\db\manager\PaymentManager.java" "%SRC%\com\scroam\db\manager\LoginManager.java" "%SRC%\com\scroam\db\listener\PlayerListener.java" "%SRC%\com\scroam\db\listener\LoginListener.java" "%SRC%\com\scroam\db\command\EconomyCommand.java" "%SRC%\com\scroam\db\command\LoginCommand.java" "%SRC%\com\scroam\db\api\ScroamDBAPI.java" "%SRC%\com\scroam\db\ScroamDB.java"

if errorlevel 1 (
    echo Compile failed
    pause
    exit /b 1
)

echo === Copying resources ===
copy "%DIR%\src\main\resources\plugin.yml" "%OUT%\" /Y
copy "%DIR%\src\main\resources\config.yml" "%OUT%\" /Y

echo === Creating JAR ===
cd /d "%DIR%"
if exist "ScroamDB-1.0.0.jar" del "ScroamDB-1.0.0.jar"
"%JAR%" cf "ScroamDB-1.0.0.jar" -C "%OUT%" .

echo === Build successful ===
dir "ScroamDB-1.0.0.jar"
pause