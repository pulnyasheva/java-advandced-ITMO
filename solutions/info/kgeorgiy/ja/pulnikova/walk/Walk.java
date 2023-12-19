package info.kgeorgiy.ja.pulnikova.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {
    static final private String BAD_FILE_HASH = "0".repeat(64);

    public static String readFileToHash(Path file, MessageDigest messageDigest) {
        try (InputStream br = Files.newInputStream(file)) {
            byte[] text = new byte[4096];
            int len;
            while ((len = br.read(text, 0, text.length)) != -1) {
                messageDigest.update(text, 0, len);
            }
            byte[] digest = messageDigest.digest();
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException ex) {
            return BAD_FILE_HASH;
        }
    }

    public static void fileProcessing(BufferedWriter bw, String name, MessageDigest messageDigest) throws IOException {
        try {
            Path pathFile = Path.of(name);
            bw.write(String.format("%s %s%n", readFileToHash(pathFile, messageDigest), name));
        } catch (InvalidPathException ex) {
            bw.write(String.format("%s %s%n", BAD_FILE_HASH, name));
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Incorrect number of arguments");
            return;
        }

        final Path pathIn;
        final Path pathOut;
        try {
            pathIn = Path.of(args[0]);
            pathOut = Path.of(args[1]);
            if (pathOut.getParent() != null && !Files.exists(pathOut.getParent())) {
                Files.createDirectory(pathOut.getParent());
            }
        } catch (IOException ex) {
            System.err.println("Failed to create directory " + ex.getMessage());
            return;
        } catch (InvalidPathException ex) {
            System.err.println(ex.getMessage());
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(pathIn, StandardCharsets.UTF_8);
             BufferedWriter bw = Files.newBufferedWriter(pathOut, StandardCharsets.UTF_8)) {
            String name;
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            while ((name = br.readLine()) != null) {
                fileProcessing(bw, name, messageDigest);
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("Algorithm not found" + ex.getMessage());
        }
    }
}
