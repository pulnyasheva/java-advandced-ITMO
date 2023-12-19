package info.kgeorgiy.ja.pulnikova.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Implementation class for {@link JarImpler} interface.
 */
public class Implementor implements JarImpler {

    public static final String OUTPUT_DIR = "temp";

    /**
     * Function for selecting a class method depending on the number of arguments
     * @param args
     * java [-jar] <class> <path>
     * <ul>
     *     <li> if there are two arguments: className, path - use {@link #implement(Class, Path)}</li>
     *     <li> if there are three arguments: -jar, className, path - use {@link #implementJar(Class, Path)}</li>
     * </ul>
     *
     */
    public static void main(String[] args){
        if (args == null || (args.length != 2 && args.length != 3)
                || args[0] == null || args[1] == null || (args.length == 3 && args[2] == null)) {
            System.err.println("Invalid parameters");
        } else {
            Implementor implementor = new Implementor();
            try {
                if (args.length == 2) {
                    implementor.implement(Class.forName(args[0]), Path.of(args[1]));
                } else {
                    System.out.println(args[1]);
                    implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Invalid name class" + e.getMessage());
            } catch (InvalidPathException e) {
                System.err.println("Invalid name path" + e.getMessage());
            } catch (ImplerException e) {
                System.err.println("Problems with implementor" + e.getMessage());
            }
        }
    }

    /**
     * Сreates a class that inherits from the passed interface at the specified path
     * @param token {@link Class} we inherit from
     * @param root {@link Path} directory where the class is created
     * @throws ImplerException
     *<ul>
     *     <li>if the token is not an interface</li>
     *     <li>if  the token is a private interface</li>
     *     <li>if it was not possible to create a directory</li>
     *     <li>if {@link IOException} when recording</li>
     *</ul>
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        checkInterface(token);
        String name = token.getSimpleName() + "Impl";
        String nameWithEnd = token.getSimpleName() + "Impl.java";
        Path absolutePath = getAbsolutePath(token, root, nameWithEnd);
        createDirectories(absolutePath);
        try (BufferedWriter wr = Files.newBufferedWriter(absolutePath)) {
            wr.write(createPackage(token));
            wr.newLine();
            wr.newLine();
            wr.write(createHeaderClass(token, name));
            wr.newLine();
            wr.newLine();
            for (Method method : getMethods(token)) {
                wr.write(createMethod(method));
                wr.newLine();
                wr.newLine();
            }
            wr.write("}");
        } catch (IOException e) {
            throw new ImplerException("Failed to write a class", e);
        }
    }

    /**
     * Checks that this is an interface, and it is not private
     * @param token {@link Class} which needs to be checked
     * @throws ImplerException
     * if the transferred token failed verification
     */

    private void checkInterface(Class<?> token) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Invalid token");
        }
    }

    /**
     * Сreates the path to the class that we want to create in the directory
     * @param token {@link Class} which we will create
     * @param root {@link Path} directory
     * @param name {@link String} name class
     * @return {@link Path} where the class will lie
     */
    private Path getAbsolutePath(Class<?> token, Path root, String name) {
        return root.resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(name);
    }

    /**
     * Creates directories on the specified {@link Path}
     * @param absolutePath the specified {@link Path}
     * @throws ImplerException
     * if it was not possible to create a directory
     */
    private void createDirectories(Path absolutePath) throws ImplerException {
        if (absolutePath.getParent() != null && !Files.exists(absolutePath.getParent())) {
            try {
                Files.createDirectories(absolutePath.getParent());
            } catch (IOException e) {
                throw new ImplerException("Failed to create directory" + absolutePath, e);
            }
        }
    }

    /**
     * Creates a package of the specified {@link Class}
     * @param token the specified {@link Class}
     * @return package name, if it exists otherwise ""
     */
    private String createPackage(Class<?> token) {
        if (!token.getPackageName().equals("")) {
            return token.getPackage().toString() + ";";
        }
        return "";
    }

    /**
     * Creates the header of the specified file with the specified name
     * @param token {@link Class} who needs a header
     * @param name {@link String} the specified name
     * @return {@link String} class header with name and implements token
     */
    private String createHeaderClass(Class<?> token, String name) {
        return "public class " +
                name +
                " implements " +
                token.getCanonicalName() +
                " {";
    }

    /**
     * Gets all abstract methods of the {@link Class}
     * @param token {@link Class} from which methods are taken
     * @return {@link List} all abstract methods
     */
    private List<Method> getMethods(Class<?> token) {
        return Arrays.stream(token.getMethods())
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .collect(Collectors.toList());
    }

    /**
     * Returns a public method with the default value returned by default
     * @param method {@link Method} which needs to be created
     * @return method as a {@link String}
     */
    private String createMethod(Method method) {
        return "    public " +
                method.getReturnType().getCanonicalName() +
                " " +
                method.getName() +
                "(" +
                createParameters(method) +
                ")" +
                "{" +
                System.lineSeparator() +
                "        return" +
                createReturn(method.getReturnType()) +
                ";" +
                System.lineSeparator() +
                "    }";
    }

    /**
     * Returns a string of parameters with their types and names separated by commas for the passed method
     * @param method the method for which parameters are needed
     * @return string a comma-separated string with parameters
     */
    private String createParameters(Method method) {
        return Arrays.stream(method.getParameters())
                .map(typeParameters ->
                        typeParameters.getType().getCanonicalName() + " " + typeParameters.getName())
                .collect(Collectors.joining(", "));
    }

    /**
     * returns the default return value for the passed class
     * @param typeReturn for which the default return value is needed
     * @return {@link String}
     * <ul>
     *     <li>if our class is booleen - false</li>
     *     <li>if our class is void - ""</li>
     *     <li>if our class is primitive - 0</li>
     *     <Li>in all other cases - null</Li>
     * </ul>
     */
    private String createReturn(Class<?> typeReturn) {
        if (typeReturn.equals(boolean.class)) {
            return " false";
        } else if (typeReturn.equals(void.class)) {
            return "";
        } else if (typeReturn.isPrimitive()) {
            return " 0";
        } else {
            return " null";
        }
    }

    /**
     * Сreates a jar file of the class inherited from the passed interface at the specified path
     * Creates a class inherited from the interface in a temporary directory and then moves it along the desired path
     * @param token type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException
     * <ul>
     *     <li>if the token is not an interface</li>
     *     <li>if  the token is a private interface</li>
     *     <li>if it was not possible to create a directory</li>
     *     <li>if {@link IOException} when recording</li>
     *     <li>if it was not possible to create a temporary directory</li>
     *     <li>if the file could not be compiled</li>
     *</ul>
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        createDirectories(jarFile);
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), OUTPUT_DIR);
        } catch (IOException e) {
            throw new ImplerException("Failed to create temp directory", e);
        }
        try {
            implement(token, tempDir);
            compileFile(token, tempDir);
            createJar(token, jarFile, tempDir);
        } finally {
            clean(tempDir);
        }
    }

    /**
     * Object {@link SimpleFileVisitor}
     */
    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Clears the directory at the specified path
     * @param root {@link Path} where the directory is located
     * @throws ImplerException
     * if the directory could not be cleared
     */
    private void clean(Path root) throws ImplerException {
        if (Files.exists(root)) {
            try {
                Files.walkFileTree(root, DELETE_VISITOR);
            } catch (IOException e) {
                throw new ImplerException("Failed delete files", e);
            }
        }
    }

    /**
     * Compiles the passed class that lies on the passed path using {@link  JavaCompiler}
     * @param token {@link Class} which needs to be compiled
     * @param root {@link Path} where is the {@link Class}
     * @throws ImplerException
     * if a compilation error occurred
     */
    private void  compileFile(Class<?> token, Path root) throws ImplerException {
        String name = token.getSimpleName() + "Impl.java";
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String classpath = root + File.pathSeparator + getClassPath(token);
        String[] args = new String[]{"-encoding","UTF-8", "-cp", classpath,
                getAbsolutePath(token, root, name).toString()};
        if (compiler == null || compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Failed to compile the file");
        }
    }

    /**
     * Returns {@link java.net.URI} for {@link Class}
     * @param token {@link Class} for which we return URI
     * @return {@link String} URI for class
     * @throws ImplerException
     * if {@link URISyntaxException}
     */
    private String getClassPath(Class<?> token) throws ImplerException {
        try {
            return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new ImplerException("Failed get path of class", e);
        }
    }

    /**
     * Creates a jar file with a manifest, where the author and version are specified, the jar lies on the specified path.
     * The compiled files are located at the specified path
     * @param token {@link Class} which needs to be compiled
     * @param root {@link Path} where will it lie jar
     * @param tempDir {@link Path} where are the compiled files
     * @throws ImplerException
     * if it was not possible to write jar file
     */
    private void createJar(Class<?> token, Path root, Path tempDir) throws ImplerException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Pulnikova Anastasia");
        String name = token.getSimpleName() + "Impl.class";
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(root), manifest)) {
            jarOutputStream.putNextEntry(
                    new JarEntry(token.getPackageName().replace('.', '/')
                            + '/' + token.getSimpleName() + "Impl.class"));
            Files.copy(getAbsolutePath(token, tempDir, name), jarOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Failed to write jar", e);
        }
    }
}


