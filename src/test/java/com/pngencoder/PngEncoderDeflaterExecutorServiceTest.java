package com.pngencoder;

import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PngEncoderDeflaterExecutorServiceTest {
    @Test
    public void getInstanceReturnsSameInstance() {
        ExecutorService expected = PngEncoderDeflaterExecutorService.getInstance();
        ExecutorService actual = PngEncoderDeflaterExecutorService.getInstance();
        assertThat(actual, is(expected));
    }
}
