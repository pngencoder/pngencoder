package com.pngencoder;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class PngEncoderBufferedImageConverterTest {
    @Test
    public void createFromIntRgb() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_RGB);
        final int[] data = PngEncoderBufferedImageConverter.getDataBufferInt(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromIntRgb(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFromIntArgb() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB);
        final int[] data = PngEncoderBufferedImageConverter.getDataBufferInt(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromIntArgb(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFromIntArgbPre() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB_PRE);
        final int[] data = PngEncoderBufferedImageConverter.getDataBufferInt(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromIntArgbPre(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFromIntBgr() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_BGR);
        final int[] data = PngEncoderBufferedImageConverter.getDataBufferInt(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromIntBgr(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFrom3ByteBgr() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_3BYTE_BGR);
        final byte[] data = PngEncoderBufferedImageConverter.getDataBufferByte(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFrom3ByteBgr(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFrom4ByteAbgr() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_4BYTE_ABGR);
        final byte[] data = PngEncoderBufferedImageConverter.getDataBufferByte(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFrom4ByteAbgr(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFrom4ByteAbgrPre() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_4BYTE_ABGR_PRE);
        final byte[] data = PngEncoderBufferedImageConverter.getDataBufferByte(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFrom4ByteAbgrPre(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFromUshort565Rgb() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_USHORT_565_RGB);
        final short[] data = PngEncoderBufferedImageConverter.getDataBufferUShort(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromUshort565Rgb(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFromUshort555Rgb() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_USHORT_555_RGB);
        final short[] data = PngEncoderBufferedImageConverter.getDataBufferUShort(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromUshort555Rgb(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFromByteGray() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_BYTE_GRAY);
        final byte[] data = PngEncoderBufferedImageConverter.getDataBufferByte(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromByteGray(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFromUshortGray() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_USHORT_GRAY);
        final short[] data = PngEncoderBufferedImageConverter.getDataBufferUShort(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromUshortGray(data, width, height);
        assertEquals(actual, expected);
    }

    @Test
    public void createFromByteBinary() {
        final BufferedImage expected = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_BYTE_BINARY);
        final byte[] data = PngEncoderBufferedImageConverter.getDataBufferByte(expected).getData();
        final int width = expected.getWidth();
        final int height = expected.getHeight();
        final BufferedImage actual = PngEncoderBufferedImageConverter.createFromByteBinary(data, width, height);
        assertEquals(actual, expected);
    }

    private static void assertEquals(BufferedImage actual, BufferedImage expected) {
        assertThat(actual.getWidth(), is(expected.getWidth()));
        assertThat(actual.getHeight(), is(expected.getHeight()));
        for (int y = 0; y < actual.getWidth(); y++) {
            for (int x = 0; x < actual.getWidth(); x++) {
                assertThat(actual.getRGB(x, y), is(expected.getRGB(x, y)));
            }
        }
    }

    @Test
    public void ensureTypeReturnsSameForSameType() {
        BufferedImage original = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB);
        BufferedImage ensured = PngEncoderBufferedImageConverter.ensureType(original, PngEncoderBufferedImageType.TYPE_INT_ARGB);
        assertThat(original, is(ensured));
    }

    @Test
    public void ensureTypeReturnsDifferentForDifferentType() {
        BufferedImage original = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB);
        BufferedImage ensured = PngEncoderBufferedImageConverter.ensureType(original, PngEncoderBufferedImageType.TYPE_USHORT_GRAY);
        assertThat(original, is(not(ensured)));
    }

    @Test
    public void copyTypeReturnsDifferentForSameType() {
        BufferedImage original = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB);
        BufferedImage ensured = PngEncoderBufferedImageConverter.copyType(original, PngEncoderBufferedImageType.TYPE_INT_ARGB);
        assertThat(original, is(not(ensured)));
    }

    @Test
    public void copyTypeReturnsDifferentForDifferentType() {
        BufferedImage original = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB);
        BufferedImage ensured = PngEncoderBufferedImageConverter.copyType(original, PngEncoderBufferedImageType.TYPE_USHORT_GRAY);
        assertThat(original, is(not(ensured)));
    }
}
