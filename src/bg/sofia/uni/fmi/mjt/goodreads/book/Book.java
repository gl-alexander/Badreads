package bg.sofia.uni.fmi.mjt.goodreads.book;

import java.util.List;

public record Book(String id, String title, List<String> authors) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('"').append(title).append('"');
        sb.append(" - ");
        if (authors != null) {
            sb.append(String.join(", ", authors));
        } else {
            sb.append("UNKNOWN");
        }
        return sb.toString();
    }
}
