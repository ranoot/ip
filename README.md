# Turing project template

This is a project template for a greenfield Java project. Given below are instructions on how to use it.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/turing/Turing.java` file, right-click it, and choose `Run Turing.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see something like the below as the output:
   ```
    _____ _   _ ____  ___ _   _  ____ 
   |_   _| | | |  _ \|_ _| \ | |/ ___|
     | | | | | | |_) || ||  \| | |  _ 
     | | | |_| |  _ < | || |\  | |_| |
     |_|  \___/|_| \_\___|_| \_|\____|
   ```

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

## Building an executable JAR

Prerequisite: JDK 25 on your `PATH`.

From the project root:

```
./build-jar.sh
```

This compiles everything under `src/main/java` and packages it into
`build/turing.jar`, recording `turing.Turing` in the JAR's manifest as the class
to start. Run the packaged chatbot with:

```
java -jar build/turing.jar
```

The JAR is self-contained, so it can be copied anywhere and run on any machine
with a JDK 25 runtime.

Without a POSIX shell, the same two steps are:

```
javac -d build/classes src/main/java/turing/*.java src/main/java/turing/task/*.java
jar --create --file build/turing.jar --main-class turing.Turing -C build/classes .
```

**Note:** the chatbot saves your tasks to `data/turing.txt` *relative to the
folder you run it from*, not to wherever the JAR sits. Run it from the folder
you want that data in. `build/` and `data/` are both git-ignored, being a build
product and the user's own data rather than source.
