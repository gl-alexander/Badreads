package bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater;

public interface Updater {
    void update(String data);

    String getData();
}
