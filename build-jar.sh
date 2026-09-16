#!/bin/sh
# Builds build/turing.jar, an executable JAR holding the whole chatbot.
#
#   ./build-jar.sh            build it
#   java -jar build/turing.jar    run it
#
# Everything is written under build/, which git ignores: a JAR is a build
# product rather than source, and it has to be rebuilt whenever the code
# changes.

# Stop at the first failure, so a compile error is not followed by a JAR
# quietly built from whatever classes were left over from last time.
set -e

CLASSES_DIR=build/classes
JAR_FILE=build/turing.jar

rm -rf "$CLASSES_DIR" "$JAR_FILE"
mkdir -p "$CLASSES_DIR"

# Compile every source file under the source root. The package folders are
# recreated under the classes folder, which is what the JAR needs.
find src/main/java -name '*.java' -print0 | xargs -0 javac -d "$CLASSES_DIR"

# --main-class records the entry point in the JAR's manifest, which is what
# lets "java -jar" start the chatbot without being told the class name.
jar --create --file "$JAR_FILE" --main-class turing.Turing -C "$CLASSES_DIR" .

echo "Built $JAR_FILE"
echo "Run it with: java -jar $JAR_FILE"
