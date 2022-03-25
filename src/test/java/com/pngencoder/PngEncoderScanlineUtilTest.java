package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PngEncoderScanlineUtilTest {
    @Test
    public void getIntRgbSize() {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_RGB);
        final byte[] data = PngEncoderScanlineUtil.get(bufferedImage);
        final int actual = data.length;
        final int expected = bufferedImage.getHeight() * (bufferedImage.getWidth() * 3 + 1);
        assertThat(actual, is(expected));
    }

    @Test
    public void getIntArgbSize() {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB);
        final byte[] data = PngEncoderScanlineUtil.get(bufferedImage);
        final int actual = data.length;
        final int expected = bufferedImage.getHeight() * (bufferedImage.getWidth() * 4 + 1);
        assertThat(actual, is(expected));
    }

    @Test
    public void getIntBgr() {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_INT_BGR, false);
    }

    @Test
    public void get3ByteBgr() {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_3BYTE_BGR, false);
    }

    @Test
    public void get4ByteAbgr() {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_4BYTE_ABGR, true);
    }

    @Test
    public void getByteGray() {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_BYTE_GRAY, false);
    }

    @Test
    public void getUshortGray() {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_USHORT_GRAY, false);
    }

    @Test
    public void getBinary() {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_BYTE_BINARY, false);
    }

    private void assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType type, boolean alpha) {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(type);
        final BufferedImage bufferedImageEnsured = PngEncoderBufferedImageConverter.ensureType(bufferedImage, alpha ? PngEncoderBufferedImageType.TYPE_INT_ARGB : PngEncoderBufferedImageType.TYPE_INT_RGB);
        final byte[] actual = PngEncoderScanlineUtil.get(bufferedImage);
        final byte[] expected = PngEncoderScanlineUtil.get(bufferedImageEnsured);
        assertThat(actual, is(expected));
    }
}
