@echo off
echo Compiling all Java files...
javac -cp ".;C:\Program Files\Java\postgresql-42.6.0.jar" src\*.java

echo Starting Server (Main.java) in a new window...
start "" cmd /k java -cp ".;C:\Program Files\Java\postgresql-42.6.0.jar" src.Main

timeout /t 5 >nul

echo Starting Client (Account.java)...
java -cp ".;C:\Program Files\Java\postgresql-42.6.0.jar" src.Account

echo Cleaning up compiled class files...
del /s /q src\*.class

echo Done.