package bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

public class FileUpdater implements Updater {
    private final File file;

    public FileUpdater(File file) {
        this.file = file;
    }

    @Override
    public void update(String data) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().collect(Collectors.joining());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
