package bg.sofia.uni.fmi.mjt.goodreads.book;

import com.google.gson.annotations.Expose;

import java.util.List;

public record BookDetails(@Expose String id, @Expose String title, @Expose List<String> authors,
                          @Expose String description, @Expose int pageCount, int publishedDate,
                          @Expose List<String> categories, @Expose int averageRating, @Expose int ratingsCount) {
    private static final String DENOMINATOR = " - ";
    private static final int LINE_LENGHT = 100;
    private static final List<String> HTML_TAGS = List.of("p", "i", "b", "br");

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Title - ").append("\"").append(title).append("\"");
        addLine(sb, "Authors", String.join(", ", authors));
        addLine(sb, "Description", wrapString(removeTags(description)));
        if (pageCount > 0) {
            addLine(sb, "Page count", String.valueOf(pageCount));
        }
        if (publishedDate > 0) {
            addLine(sb, "Publish date", String.valueOf(publishedDate));
        }
        if (categories != null) {
            addLine(sb, "Categories", String.join(", ", categories));
        }
        if (averageRating > 0 && ratingsCount > 0) {
            addLine(sb, "Rating", String.format("%d, %d total ratings", averageRating, ratingsCount));
        }
        return sb.toString();
    }

    private String wrapString(String s) {
        StringBuilder sb = new StringBuilder(s);

        int i = 0;
        while (i + LINE_LENGHT < sb.length() && (i = sb.lastIndexOf(" ", i + LINE_LENGHT)) != -1) {
            sb.replace(i, i + 1, System.lineSeparator());
        }

        return sb.toString();
    }

    private String removeTags(String s) {
        for (String tag : HTML_TAGS) {
            String replacement = "";
            if (tag.equals("br")) {
                replacement = System.lineSeparator();
            }
            s = s.replace("<" + tag + ">", replacement);
            s = s.replace("</" + tag + ">", replacement);
        }
        return s;
    }

    private void addLine(StringBuilder sb, String token, String value) {
        if (value == null) {
            return;
        }
        sb.append(System.lineSeparator()).append(token).append(DENOMINATOR).append(value);
    }
}
