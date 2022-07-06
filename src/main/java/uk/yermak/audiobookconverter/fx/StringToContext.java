package uk.yermak.audiobookconverter.fx;

import org.apache.commons.io.FilenameUtils;
import uk.yermak.audiobookconverter.book.Chapter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


/**
 * Util class which function is updating context in {@link ChapterEditor#editChapter()} method,
 * so we can use printf like syntax for template naming chapter names.
 *
 * @author patsh
 */
public class StringToContext {

    /**
     * Special tokens used in creating chapter naming template.
     */
    public final static Set<String> SPECIAL_TOKENS;

    /**
     * Mapping between specials tokens at functions that map chapter object to some string.
     */
    public final static Map<String, Function<Chapter, Object>> SPECIAL_TOKEN_TO_FUNCTION;

    static {
        SPECIAL_TOKEN_TO_FUNCTION = new HashMap<>();
        SPECIAL_TOKEN_TO_FUNCTION.put("%BOOK_NUMBER%",
                c -> String.valueOf(c.getPart().getBook().getBookInfo().bookNumber()));
        SPECIAL_TOKEN_TO_FUNCTION.put("%comment-0%",
                chapter -> chapter.getMedia().get(0).getBookInfo().comment());
        SPECIAL_TOKEN_TO_FUNCTION.put("%year%",
                chapter -> chapter.getMedia().get(0).getBookInfo().year());
        SPECIAL_TOKEN_TO_FUNCTION.put("%genre%",
                chapter -> chapter.getMedia().get(0).getBookInfo().genre());
        SPECIAL_TOKEN_TO_FUNCTION.put("%album%",
                chapter -> chapter.getMedia().get(0).getBookInfo().series());
        SPECIAL_TOKEN_TO_FUNCTION.put("%album_artist%",
                chapter -> chapter.getMedia().get(0).getBookInfo().narrator());
        SPECIAL_TOKEN_TO_FUNCTION.put("%artist%",
                chapter -> chapter.getMedia().get(0).getBookInfo().writer());
        SPECIAL_TOKEN_TO_FUNCTION.put("%title%",
                chapter -> chapter.getMedia().get(0).getBookInfo().title());
        SPECIAL_TOKEN_TO_FUNCTION.put("%BOOK_TITLE%",
                c -> c.getPart().getBook().getBookInfo().title());
        SPECIAL_TOKEN_TO_FUNCTION.put("%DURATION%",
                c -> Duration.ofMillis(c.getDuration()));
        SPECIAL_TOKEN_TO_FUNCTION.put("%file_name%",
                c -> FilenameUtils.getBaseName(c.getMedia().get(0).getFileName()));
        SPECIAL_TOKEN_TO_FUNCTION.put("%CHAPTER_TEXT%",
                c -> "Chapter");
        SPECIAL_TOKEN_TO_FUNCTION.put("%CHAPTER_NUMBER%",
                c -> c.getNumber());

        SPECIAL_TOKENS = SPECIAL_TOKEN_TO_FUNCTION.keySet();
    }



    public static Map<String, Function<Chapter, Object>> updateContextUsingPrintf(Map<String, Function<Chapter, Object>> context, String printf) {
        Function<Chapter, Object> exp = getFunction(printf);

        context.put("CUSTOM_TITLE", exp);
        return context;
    }

    /**
     * Main method for generating function from string with special tokens.
     * <p></p>
     * For example string - "%title% - chapter" will
     * construct function that combines function mapped in {@link #SPECIAL_TOKEN_TO_FUNCTION} with string "- chapter".
     * And put it in "CUSTOM_TITLE" value in context object.
     *
     * @param printf String for formatting chapter title. For example - "%title% - chapter"
     * @return Function combining functions and strings
     */

    public static Function<Chapter, Object> getFunction(String printf){
        String[] tokens = printf.split(" ");
        ArrayList<Function<Chapter, Object>> functions = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        values.add("");
        for (String token : tokens) {
            if (SPECIAL_TOKENS.contains(token)) {
                functions.add(SPECIAL_TOKEN_TO_FUNCTION.get(token));
            } else {
                values.add(token);
            }
        }

        Function<Chapter, Object> exp = chapter -> {
            int n = Math.min(functions.size(), values.size());
            int i = 0;
            StringBuilder sb = new StringBuilder();
            while (i < n) {
                sb.append(values.get(i));
                sb.append(" ");
                sb.append(functions.get(i).apply(chapter));
                sb.append(" ");
                i++;
            }

            while (i < functions.size()) {
                sb.append(functions.get(i).apply(chapter));
                i++;
            }
            while (i < values.size()) {
                sb.append(values.get(i));
                i++;
            }
            return sb.toString();
        };
        return exp;
    }
}
