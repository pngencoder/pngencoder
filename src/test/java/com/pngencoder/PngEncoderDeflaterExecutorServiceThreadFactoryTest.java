package com.pngencoder;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PngEncoderDeflaterExecutorServiceThreadFactoryTest {
    @Test
    public void getInstanceReturnsSameInstance() {
        PngEncoderDeflaterExecutorServiceThreadFactory expected = PngEncoderDeflaterExecutorServiceThreadFactory.getInstance();
        PngEncoderDeflaterExecutorServiceThreadFactory actual = PngEncoderDeflaterExecutorServiceThreadFactory.getInstance();
        assertThat(actual, is(expected));
    }

    @Test
    public void daemonIsTrue() {
        Thread thread = newThread();
        boolean actual = thread.isDaemon();
        boolean expected = true;

        assertThat(actual, is(expected));
    }

    @Test
    public void nameIsCustom() {
        Thread thread = newThread();
        String actual = thread.getName();

        assertThat(actual, is("PngEncoder Deflater (0)"));
    }

    private static Thread newThread() {
        PngEncoderDeflaterExecutorServiceThreadFactory threadFactory = new PngEncoderDeflaterExecutorServiceThreadFactory();
        return threadFactory.newThread(() -> {
        });
    }
}
