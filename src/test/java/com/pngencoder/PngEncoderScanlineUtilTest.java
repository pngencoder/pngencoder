package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PngEncoderScanlineUtilTest {
    @Test
    public void getIntRgbSize() throws IOException {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_RGB);
        final byte[] data = PngEncoderScanlineUtil.get(bufferedImage);
        final int actual = data.length;
        final int expected = bufferedImage.getHeight() * (bufferedImage.getWidth() * 3 + 1);
        assertThat(actual, is(expected));
    }

    @Test
    public void getIntArgbSize() throws IOException {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB);
        final byte[] data = PngEncoderScanlineUtil.get(bufferedImage);
        final int actual = data.length;
        final int expected = bufferedImage.getHeight() * (bufferedImage.getWidth() * 4 + 1);
        assertThat(actual, is(expected));
    }

    @Test
    public void getIntBgr() throws IOException {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_INT_BGR, false);
    }

    @Test
    public void get3ByteBgr() throws IOException {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_3BYTE_BGR, false);
    }

    @Test
    public void get4ByteAbgr() throws IOException {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_4BYTE_ABGR, true);
    }

    @Test
    public void getBinary() throws IOException {
        assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType.TYPE_BYTE_BINARY, false);
    }

    @Test
    public void testCustomByteRGBA() throws IOException {
        final BufferedImage sourceImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_4BYTE_ABGR);
        BufferedImage imgRGBA = CustomDataBuffers.create8BitRGBA(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getColorModel());
        Graphics2D graphics = imgRGBA.createGraphics();
        graphics.drawImage(sourceImage, 0, 0, null);
        graphics.dispose();
        final byte[] actual = PngEncoderScanlineUtil.get(sourceImage);
        final byte[] expected = PngEncoderScanlineUtil.get(imgRGBA);
        assertThat(actual, is(expected));
    }

    @Test
    public void testCustomUShortRGBA() throws IOException {
        final BufferedImage sourceImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_4BYTE_ABGR);
        BufferedImage imgRGBA = CustomDataBuffers.create16BitRGBA(sourceImage.getWidth(), sourceImage.getHeight());
        Graphics2D graphics = imgRGBA.createGraphics();
        graphics.drawImage(sourceImage, 0, 0, null);
        graphics.dispose();
        final byte[] actual = PngEncoderScanlineUtil.get(sourceImage);
        final byte[] expected = PngEncoderScanlineUtil.get(imgRGBA);
        assertThat(actual.length * 2 - sourceImage.getHeight(), is(expected.length));
    }

    @Test
    public void testCustomIntRGBA() throws IOException {
        final BufferedImage sourceImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_4BYTE_ABGR);
        BufferedImage imgRGBA = CustomDataBuffers.createInt8BitRGBA(sourceImage.getWidth(), sourceImage.getHeight());
        Graphics2D graphics = imgRGBA.createGraphics();
        graphics.drawImage(sourceImage, 0, 0, null);
        graphics.dispose();
        final byte[] actual = PngEncoderScanlineUtil.get(sourceImage);
        final byte[] expected = PngEncoderScanlineUtil.get(imgRGBA);
        assertThat(actual, is(expected));
    }

    private void assertThatScanlineOfTestImageEqualsIntRgbOrArgb(PngEncoderBufferedImageType type, boolean alpha) throws IOException {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(type);
        final BufferedImage bufferedImageEnsured = PngEncoderBufferedImageConverter.ensureType(bufferedImage, alpha ? PngEncoderBufferedImageType.TYPE_INT_ARGB : PngEncoderBufferedImageType.TYPE_INT_RGB);
        final byte[] actual = PngEncoderScanlineUtil.get(bufferedImage);
        final byte[] expected = PngEncoderScanlineUtil.get(bufferedImageEnsured);
        assertThat(actual, is(expected));
    }
}
