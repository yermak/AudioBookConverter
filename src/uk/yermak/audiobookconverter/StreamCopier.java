package uk.yermak.audiobookconverter;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;


/**
 * Created by Yermak on 27-Dec-17.
 */
public class StreamCopier implements Callable<Long> {

    private InputStream in;
    private OutputStream out;


    public StreamCopier(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public Long call() throws Exception {
        long count = 0L;
        byte[] buffer = new byte[4096];
        for (int n; -1 != (n = in.read(buffer)); count += (long) n) {
            out.write(buffer, 0, n);
        }
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        return count;
    }
}
