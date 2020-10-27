package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PngEncoderIdatChunksOutputStreamTest {
    @Test
    public void assertThatIdatChunkStartsWithContentLength() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PngEncoderIdatChunksOutputStream idatChunksOutputStream = new PngEncoderIdatChunksOutputStream(byteArrayOutputStream);
        final byte[] content = {1, 2, 3};
        idatChunksOutputStream.write(content);
        idatChunksOutputStream.flush();
        final byte[] idatChunkBytes = byteArrayOutputStream.toByteArray();
        final int actual = ByteBuffer.wrap(idatChunkBytes, 0, 4).asIntBuffer().get();
        final int expected = content.length;
        assertThat(actual, is(expected));
    }

    @Test
    public void assertThatIdatChunkContainsIdatStringBytes() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PngEncoderIdatChunksOutputStream idatChunksOutputStream = new PngEncoderIdatChunksOutputStream(byteArrayOutputStream);
        final byte[] content = {1, 2, 3};
        idatChunksOutputStream.write(content);
        idatChunksOutputStream.flush();
        final byte[] idatChunkBytes = byteArrayOutputStream.toByteArray();
        final byte[] actual = Arrays.copyOfRange(idatChunkBytes, 4, 8);
        final byte[] expected = PngEncoderIdatChunksOutputStream.IDAT_BYTES;
        assertThat(actual, is(expected));
    }
}
