package bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater;

public class StringUpdater implements Updater {
    private String data;
    public StringUpdater(String data) {
        this.data = data;
    }

    @Override
    public void update(String data) {
        this.data = new String(data);
    }

    @Override
    public String getData() {
        return this.data;
    }
}
