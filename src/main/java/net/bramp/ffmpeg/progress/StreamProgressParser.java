package net.bramp.ffmpeg.progress;

import java.io.*;
import java.nio.charset.Charset;


public class StreamProgressParser {

    final ProgressListener listener;

    public StreamProgressParser(ProgressListener listener) {
        this.listener = listener;
    }

    private static BufferedReader wrapInBufferedReader(Reader reader) {
        if (reader instanceof BufferedReader) {
            return (BufferedReader) reader;
        }

        return new BufferedReader(reader);
    }

    public void processStream(InputStream stream) throws IOException {
        processReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
    }

    public void processReader(Reader reader) throws IOException {
        final BufferedReader in = wrapInBufferedReader(reader);

        String line;
        Progress p = new Progress();
        while ((line = in.readLine()) != null) {
            if (p.parseLine(line)) {
                listener.progress(p);
                p = new Progress();
            }
        }
    }
}
