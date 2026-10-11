package uk.yermak.audiobookconverter.loaders;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.Utils;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.MediaInfoBean;
import uk.yermak.audiobookconverter.book.Track;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FlacLoader {

    static void parseCueChapters(MediaInfoBean mediaInfo) throws IOException {
        String filename = mediaInfo.getFileName();
        File file = new File(FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename) + ".cue");
        if (file.exists()) {
            // the sidecar wins over chapters ffmpeg made from an embedded CUESHEET block: those carry no titles,
            // and keeping both would list (and convert) every track twice
            mediaInfo.getBookInfo().tracks().clear();
            parseCue(mediaInfo, readCue(file));
        }
    }

    /** UTF-8 when the file decodes as UTF-8; otherwise the system's ANSI code page, which older rippers write in. */
    static String readCue(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        try {
            return StringUtils.removeStart(StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString(), "﻿");
        } catch (CharacterCodingException e) {
            // ponytail: a cue made under another code page (cp1251 cue on a cp1252 system) still comes out garbled
            return new String(bytes, Charset.forName(System.getProperty("native.encoding"), StandardCharsets.UTF_8));
        }
    }

    static void parseCue(MediaInfoBean mediaInfo, String cue) {
        AudioBookInfo bookInfo = mediaInfo.getBookInfo();
        List<Track> tracks = bookInfo.tracks();
        for (String line : StringUtils.split(cue, "\n")) {
            // the keyword is the first word ("REM GENRE" counts as GENRE): "Back on TRACK" in a title is not a new track
            String command = StringUtils.removeStart(line.trim(), "REM ").trim();
            String keyword = StringUtils.substringBefore(command, " ");
            String value = Utils.cleanText(StringUtils.substringAfter(command, " "));
            switch (keyword) {
                case "GENRE" -> {
                    if (tracks.isEmpty()) bookInfo.genre().set(value);
                }
                case "DATE" -> {
                    if (tracks.isEmpty()) bookInfo.year().set(value);
                }
                case "TITLE" -> {
                    if (tracks.isEmpty()) bookInfo.title().set(value);
                    else tracks.getLast().setTitle(value);
                }
                case "PERFORMER" -> {
                    if (tracks.isEmpty()) bookInfo.narrator().set(value);
                    else tracks.getLast().setWriter(value);
                }
                case "TRACK" -> tracks.add(new Track(value));
                case "INDEX" -> {
                    if (value.startsWith("01 ") && !tracks.isEmpty()) {
                        long time = parseCueTime(value.substring(3));
                        if (tracks.size() > 1) tracks.get(tracks.size() - 2).setEnd(time);
                        tracks.getLast().setStart(time);
                    }
                }
                default -> {
                }
            }
        }
        if (!bookInfo.tracks().isEmpty()) {
            bookInfo.tracks().get(bookInfo.tracks().size() - 1).setEnd(mediaInfo.getDuration());
        }
    }

    private static long parseCueTime(String substring) {
        String cleanText = Utils.cleanText(substring);
        String[] split = cleanText.split(":");
        return 1000 * (Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1])) + Integer.parseInt(split[2]) * 1000 / 75;
    }
}
