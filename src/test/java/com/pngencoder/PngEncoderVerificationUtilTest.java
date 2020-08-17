package com.pngencoder;

import org.junit.Test;

public class PngEncoderVerificationUtilTest {
    @Test
    public void verifyCompressionLevelAcceptsMinusOne() {
        PngEncoderVerificationUtil.verifyCompressionLevel(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyCompressionLevelRejectsMinusTwo() {
        PngEncoderVerificationUtil.verifyCompressionLevel(-2);
    }

    @Test
    public void verifyCompressionLevelAcceptsNine() {
        PngEncoderVerificationUtil.verifyCompressionLevel(9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyCompressionLevelRejectsTen() {
        PngEncoderVerificationUtil.verifyCompressionLevel(10);
    }

    @Test
    public void verifyChunkTypeAcceptsIDAT() {
        PngEncoderVerificationUtil.verifyChunkType("IDAT");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyChunkTypeRejectsLorem() {
        PngEncoderVerificationUtil.verifyChunkType("Lorem");
    }
}
