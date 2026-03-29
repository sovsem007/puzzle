#!/bin/sh
#
# Gradle wrapper script for Unix systems

APP_HOME="${APP_HOME:-`dirname "$0"`}"
APP_HOME=`cd "$APP_HOME" && pwd`
APP_NAME="Gradle"

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
fi

# Classpath
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Execute Gradle
exec "$JAVACMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
