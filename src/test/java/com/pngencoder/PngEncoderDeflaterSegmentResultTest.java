package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.util.zip.Adler32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PngEncoderDeflaterSegmentResultTest {
    @Test
    public void combineAdler32() {
        byte[] bytesAll = {1, 2, 3, 4, 5, 6, 7};
        byte[] bytes1 = {1, 2, 3};
        byte[] bytes2 = {4, 5, 6, 7};

        Adler32 bytes1Adler32 = new Adler32();
        bytes1Adler32.update(bytes1);
        long adler1 = bytes1Adler32.getValue();

        Adler32 bytes2Adler32 = new Adler32();
        bytes2Adler32.update(bytes2);
        long adler2 = bytes2Adler32.getValue();

        long actual = PngEncoderDeflaterSegmentResult.combine(adler1, adler2, bytes2.length);

        Adler32 bytesAllAdler32 = new Adler32();
        bytesAllAdler32.update(bytesAll);
        long expected = bytesAllAdler32.getValue();

        assertThat(actual, is(expected));
    }
}
