@echo off
if exist bin rmdir /s /q bin
for /r src %%f in (*.class) do del /q "%%f"
mkdir bin

REM Compile in dependency order
echo Compiling models...
javac -d bin -cp "lib/*" src/model/*.java

echo Compiling utilities...
javac -d bin -cp "lib/*" src/util/*.java

echo Compiling DAOs...
javac -d bin -cp "lib/*;bin" src/dao/*.java

echo Compiling services...
javac -d bin -cp "lib/*;bin" src/service/*.java

echo Compiling main application...
javac -d bin -cp "lib/*;bin" src/main/*.java

if not exist bin\config mkdir bin\config
copy src\config\db.properties bin\config\db.properties
echo Compilation complete.
pause
