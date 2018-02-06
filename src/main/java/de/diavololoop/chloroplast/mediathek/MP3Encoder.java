package de.diavololoop.chloroplast.mediathek;

import de.diavololoop.chloroplast.Util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by Chloroplast on 05.02.2018.
 */
public class MP3Encoder {

    private static File LAME_PATH;


    public MP3Encoder() {

        LAME_PATH = getLAMEPath();

        if (LAME_PATH == null) {
            throw new IllegalArgumentException("LAME not found in PATH");
        }

        System.out.println(LAME_PATH.getAbsolutePath());

    }

    public void encodeFile(File inFile, File outFile, Consumer<Integer> result, Consumer<Exception> exceptionCallback) {

        ProcessBuilder builder = new ProcessBuilder(LAME_PATH.getAbsolutePath(), "--quiet", "-V0", inFile.getAbsolutePath(), outFile.getAbsolutePath());



        Thread thread = new Thread(() -> {
            try {

                Process proc = builder.start();

                int returnCode = proc.waitFor();
                result.accept(returnCode);
            } catch (IOException e) {
                exceptionCallback.accept(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        });

        thread.start();
    }


    private File getLAMEPath() {

        boolean isWindows = System.getProperty("os.name").matches(".*[Ww]indows.*");

        String pathKey = isWindows ? "Path" : "PATH";
        String seperator = isWindows ? ";" : ":";

        Map<String, String> env = System.getenv();
        List<String> possibleRoots = new LinkedList<String>();

        if (!env.containsKey(pathKey)) {
            throw new IllegalStateException("Path variable is not known");
        }

        possibleRoots.addAll(Arrays.asList(env.get(pathKey).split(seperator)));

        possibleRoots.add(System.getProperty("user.dir"));

        return possibleRoots.stream()
                .map(File::new)
                .filter(File::isDirectory)
                .flatMap(f -> Arrays.stream(f.listFiles()))
                .filter(File::canExecute)
                .filter(f -> f.getName().equalsIgnoreCase("lame.exe") || f.getName().equalsIgnoreCase("lame"))
                .findFirst().orElse(null);

    }


}
