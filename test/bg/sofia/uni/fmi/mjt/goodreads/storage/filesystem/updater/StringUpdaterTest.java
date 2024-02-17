package bg.sofia.uni.fmi.mjt.goodreads.storage.filesystem.updater;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUpdaterTest {
    @Test
    public void testConstructorAndGetters() {
        // Given
        String initialData = "initialData";

        // When
        StringUpdater stringUpdater = new StringUpdater(initialData);

        // Then
        assertEquals(initialData, stringUpdater.getData());
    }

    @Test
    public void testUpdate() {
        // Given
        String initialData = new String("initial data");
        String newData = "newData";
        StringUpdater stringUpdater = new StringUpdater(initialData);

        // When
        stringUpdater.update(newData);

        // Then
        assertEquals(newData, stringUpdater.getData());
    }
}
