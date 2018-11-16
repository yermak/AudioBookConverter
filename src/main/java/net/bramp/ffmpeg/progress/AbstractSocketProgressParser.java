package net.bramp.ffmpeg.progress;


import java.io.IOException;
import java.util.concurrent.CountDownLatch;


public abstract class AbstractSocketProgressParser implements ProgressParser {

    final StreamProgressParser parser;

    Thread thread; // Thread for handling incoming connections

    public AbstractSocketProgressParser(ProgressListener listener) {
        this.parser = new StreamProgressParser(listener);
    }


    protected abstract String getThreadName();

    protected abstract Runnable getRunnable(CountDownLatch startSignal);

    /**
     * Starts the ProgressParser waiting for progress.
     *
     * @throws IllegalThreadStateException if the parser was already started.
     */
    @Override
    public synchronized void start() {
        if (thread != null) {
            throw new IllegalThreadStateException("Parser already started");
        }

        String name = getThreadName() + "(" + getUri().toString() + ")";

        CountDownLatch startSignal = new CountDownLatch(1);
        Runnable runnable = getRunnable(startSignal);

        thread = new Thread(runnable, name);
        thread.start();

        // Block until the thread has started
        try {
            startSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void stop() throws IOException {
        if (thread != null) {
            thread.interrupt(); // This unblocks processStream();

            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void close() throws IOException {
        stop();
    }
}
