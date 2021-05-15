package uk.yermak.audiobookconverter;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class GenresManager {

    private final ObservableList<String> genres = FXCollections.observableArrayList();

    public void saveGenres(AudioBookInfo bookInfo) {
        if (bookInfo != null) {
            if (StringUtils.isNotEmpty(bookInfo.genre().get()) & genres.stream().noneMatch(s -> s.equals(bookInfo.genre().get()))) {
                genres.add(bookInfo.genre().get());
            }
            Gson gson = new Gson();
            String genresString = gson.toJson(new ArrayList<>(genres));
            AppProperties.setProperty("genres", genresString);
        }
    }

    public ObservableList<String> loadGenres() {
        genres.clear();
        String genresProperty = AppProperties.getProperty("genres");
        if (genresProperty != null) {
            Gson gson = new Gson();
            ArrayList<String> list = gson.fromJson(genresProperty, ArrayList.class);
            this.genres.addAll(list.stream().sorted().collect(Collectors.toList()));
        }
        return genres;
    }

}
