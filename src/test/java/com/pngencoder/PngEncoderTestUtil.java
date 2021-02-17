package com.pngencoder;

import org.openjdk.jmh.util.NullOutputStream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Random;
import javax.imageio.ImageIO;

class PngEncoderTestUtil {
    private static final OutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

    private static final Random RANDOM = new Random();
    private static final int DEFAULT_SIDE = 128;

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

    static void encodeWithImageIO(BufferedImage bufferedImage) {
        try {
            ImageIO.write(bufferedImage, "png", NULL_OUTPUT_STREAM);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void encodeWithObjectPlanetPngEncoder(BufferedImage bufferedImage) {
        // https://www.objectplanet.com/pngencoder/
        try {
            new com.objectplanet.image.PngEncoder().encode(bufferedImage, NULL_OUTPUT_STREAM);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
