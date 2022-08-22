package com.pngencoder;

import org.openjdk.jmh.util.NullOutputStream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Random;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PngEncoderTestUtil {
    private static final OutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

    private static final Random RANDOM = new Random();
    private static final int DEFAULT_SIDE = 256;

    static BufferedImage createTestImage(PngEncoderBufferedImageType type) {
        return createTestImage(type, DEFAULT_SIDE);
    }

    static BufferedImage createTestImage(PngEncoderBufferedImageType type, int side) {
        final BufferedImage bufferedImage = new BufferedImage(side, side, PngEncoderBufferedImageType.TYPE_INT_ARGB.ordinal());
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int a = RANDOM.nextInt(256);
                int r = RANDOM.nextInt(256);
                int g = RANDOM.nextInt(256);
                int b = RANDOM.nextInt(256);
                int argb = a << 24 | r << 16 | g << 8 | b;
                bufferedImage.setRGB(x, y, argb);
            }
        }
        return PngEncoderBufferedImageConverter.ensureType(bufferedImage, type);
    }

    static BufferedImage readTestImageResource(String name) {
        try (InputStream inputStream = PngEncoderTestUtil.class.getResourceAsStream("/" + name)) {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read test image resource: " + name, e);
        }
    }

    static int encodeWithPngEncoder(BufferedImage bufferedImage) {
        return new PngEncoder()
                .withBufferedImage(bufferedImage)
                .toStream(NULL_OUTPUT_STREAM);
    }

    static int encodeWithPngEncoder(BufferedImage bufferedImage, int compressionLevel) {
        return new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(compressionLevel)
                .toStream(NULL_OUTPUT_STREAM);
    }

    static int encodeWithPngEncoderPredictorEncoding(BufferedImage bufferedImage, int compressionLevel) {
        return new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(compressionLevel)
                .withPredictorEncoding(true)
                .toStream(NULL_OUTPUT_STREAM);
    }

    static void encodeWithImageIO(BufferedImage bufferedImage) {
        try {
            ImageIO.write(bufferedImage, "png", NULL_OUTPUT_STREAM);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void assertThatImageIsEqual(BufferedImage actual, BufferedImage expected) {
        assertEquals(actual.getWidth(), expected.getWidth());
        assertEquals(actual.getHeight(), expected.getHeight());
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                int expectedPixel = expected.getRGB(x, y);
                int actualPixel = actual.getRGB(x, y);
                assertThatPixelIs(x, y, actualPixel, expectedPixel);
            }
        }
    }

    private static void assertThatPixelIs(int x, int y, int actual, int expected) {
        if (expected == actual) {
            return;
        }

        long alphaActual = actual & 0xFF000000L;
        long alphaExpected = actual & 0xFF000000L;
        if (alphaExpected == alphaActual && alphaExpected == 0) {
            // The alpha is 0, so the pixel is not visible. So we don't care
            // about the not visible rgb values. The indexed encoders flattens this
            // values to 0.
            return;
        }

        String formattedActual = formatPixel(actual);
        String formattedExpected = formatPixel(expected);
        String reason = String.format("Pixel at %d,%d Expected: %s Actually: %s", x, y, formattedExpected, formattedActual);
        throw new AssertionError(reason);
    }

    private static String formatPixel(int pixel) {
        return String.format("0x%08x", pixel);
    }
}
