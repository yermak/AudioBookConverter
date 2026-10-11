package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.*;

public class ConversionJobTest {

    @Test(timeOut = 20_000)
    public void pausedJobWaitsOnItsOwnThreadForASlot() throws Exception {
        int taken = ConversionJob.POOL_SLOTS.drainPermits(); // every pool thread busy
        try {
            AtomicReference<ProgressStatus> status = new AtomicReference<>(ProgressStatus.PAUSED);
            CompletableFuture<Boolean> acquired = CompletableFuture.supplyAsync(() -> {
                try {
                    return ConversionJob.acquireSlot(status::get);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            ConversionJob.POOL_SLOTS.release(); // a slot frees up, but the job is paused
            Thread.sleep(1500);
            assertFalse(acquired.isDone());

            status.set(ProgressStatus.IN_PROGRESS);
            assertTrue(acquired.get(5, TimeUnit.SECONDS));

            assertFalse(ConversionJob.acquireSlot(() -> ProgressStatus.CANCELLED));
        } finally {
            ConversionJob.POOL_SLOTS.drainPermits();
            ConversionJob.POOL_SLOTS.release(taken);
        }
    }
}
