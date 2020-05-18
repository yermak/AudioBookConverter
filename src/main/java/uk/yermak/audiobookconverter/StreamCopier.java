package uk.yermak.audiobookconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * Created by Yermak on 27-Dec-17.
 */
public class StreamCopier implements Callable<Long> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final InputStream in;
    private final OutputStream out;


    private StreamCopier(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public Long call() throws Exception {
        long count = 0L;
        byte[] buffer = new byte[4096];
        for (int n; -1 != (n = in.read(buffer)); count += n) {
            out.write(buffer, 0, n);
        }
        closeQuietly(in);
        closeQuietly(out);
        return count;
    }


    public static Future<Long> copy(InputStream in, OutputStream out) {
        return executorService.submit(new StreamCopier(in, out));
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }
}
