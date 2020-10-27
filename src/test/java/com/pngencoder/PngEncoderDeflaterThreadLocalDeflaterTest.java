package com.pngencoder;

import org.junit.Test;

import java.util.zip.Deflater;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

public class PngEncoderDeflaterThreadLocalDeflaterTest {
    @Test
    public void sameCompressionLevelReturnsSameInstance() {
        final Deflater expected = PngEncoderDeflaterThreadLocalDeflater.getInstance(1);
        final Deflater actual = PngEncoderDeflaterThreadLocalDeflater.getInstance(1);
        assertThat(actual, is(sameInstance(expected)));
    }

    @Test
    public void assertThatAllCompressionLevelInstancesAreGettable() {
        for (int compressionLevel = -1; compressionLevel <= 9; compressionLevel++) {
            Deflater deflater = PngEncoderDeflaterThreadLocalDeflater.getInstance(1);
            assertThat(deflater, is(notNullValue(Deflater.class)));
        }
    }
}
