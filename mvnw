#!/bin/sh
# Maven Wrapper script
# Licensed to the Apache Software Foundation (ASF)

if [ -z "$JAVA_HOME" ] ; then
  JAVACMD=`which java 2> /dev/null`
  if [ -z "$JAVACMD" ] ; then
    echo "Error: JAVA_HOME not set and java command not found in PATH." >&2
    exit 1
  fi
else
  JAVACMD="$JAVA_HOME/bin/java"
fi

MAVEN_WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
MAVEN_WRAPPER_PROPERTIES=".mvn/wrapper/maven-wrapper.properties"

if [ -f "$MAVEN_WRAPPER_JAR" ]; then
  exec "$JAVACMD" -jar "$MAVEN_WRAPPER_JAR" "$@"
else
  # Fallback to system maven
  if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
  else
    echo "Error: Maven not found. Install Maven or run: ./mvnw (requires .mvn/wrapper/maven-wrapper.jar)" >&2
    exit 1
  fi
fi
