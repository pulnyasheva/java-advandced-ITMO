set package=..\java-solutions\info\kgeorgiy\ja\pulnikova\implementor
set package_kg=info\kgeorgiy\java\advanced\implementor
set art=..\java-advanced-2023\artifacts\info.kgeorgiy.java.advanced.implementor.jar
javac -d . --class-path %art% %package%\Implementor.java
jar -cfm Implementor.jar MANIFEST.MF info\kgeorgiy\ja\pulnikova\implementor\*.class