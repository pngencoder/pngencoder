package com.pngencoder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PngEncoderVerificationUtilTest {
    @Test
    public void verifyCompressionLevelAcceptsMinusOne() {
        PngEncoderVerificationUtil.verifyCompressionLevel(-1);
    }

    @Test
    public void verifyCompressionLevelRejectsMinusTwo() {
        assertThrows(IllegalArgumentException.class, () -> PngEncoderVerificationUtil.verifyCompressionLevel(-2));
    }

    @Test
    public void verifyCompressionLevelAcceptsNine() {
        PngEncoderVerificationUtil.verifyCompressionLevel(9);
    }

    @Test
    public void verifyCompressionLevelRejectsTen() {
        assertThrows(IllegalArgumentException.class, () -> PngEncoderVerificationUtil.verifyCompressionLevel(10));
    }

    @Test
    public void verifyChunkTypeAcceptsIDAT() {
        PngEncoderVerificationUtil.verifyChunkType("IDAT");
    }

    @Test
    public void verifyChunkTypeRejectsLorem() {
        assertThrows(IllegalArgumentException.class, () -> PngEncoderVerificationUtil.verifyChunkType("Lorem"));
    }
}
