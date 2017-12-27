package com.freeipodsoftware.abc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamDumper {
    private boolean cancel;

    public StreamDumper(final InputStream inputStream) {
        Thread t = new Thread(new Runnable() {
            private BufferedInputStream bufferedIS;

            public void run() {
                byte[] buffer = new byte[1024];
                this.bufferedIS = new BufferedInputStream(inputStream);

                try {
                    while (!StreamDumper.this.cancel) {
                        this.bufferedIS.read(buffer);
                    }
                } catch (IOException e) {
                    ;
                }

            }
        });
        t.start();
    }

    public void stop() {
        this.cancel = true;
    }
}
