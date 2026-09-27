@echo off
if "%JAVA_HOME%"=="" (
  set JAVACMD=java
) else (
  set JAVACMD=%JAVA_HOME%\bin\java
)
if exist ".mvn\wrapper\maven-wrapper.jar" (
  %JAVACMD% -jar ".mvn\wrapper\maven-wrapper.jar" %*
) else (
  mvn %*
)
