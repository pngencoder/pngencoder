package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PngEncoderDeflaterExecutorServiceTest {
    @Test
    public void getInstanceReturnsSameInstance() {
        ExecutorService expected = PngEncoderDeflaterExecutorService.getInstance();
        ExecutorService actual = PngEncoderDeflaterExecutorService.getInstance();
        assertThat(actual, is(expected));
    }
}
