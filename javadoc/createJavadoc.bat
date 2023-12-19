set link=https://docs.oracle.com/en/java/javase/19/docs/api/
set package=info.kgeorgiy.ja.pulnikova.implementor
set package_kg=../java-advanced-2023/modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/
set art=../java-advanced-2023/artifacts/info.kgeorgiy.java.advanced.implementor.jar
cd ../java-solutions/
javadoc -d ../javadoc -link %link% --class-path .;%art%;../java-advanced-2023/lib/* -private %package% %package_kg%Impler.java %package_kg%JarImpler.java %package_kg%ImplerException.java