@echo off
echo Cleaning and rebuilding CivicConnect...
echo.

REM Delete target directory
if exist target (
    echo Deleting target directory...
    rmdir /s /q target
)

echo.
echo Please rebuild the project in your IDE (Eclipse):
echo 1. Right-click on the project
echo 2. Select "Run As" -^> "Maven clean"
echo 3. Then "Run As" -^> "Maven install"
echo.
echo Or run: mvn clean install
echo.
pause
