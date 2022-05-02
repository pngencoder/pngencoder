package com.pngencoder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Element;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PngEncoderTest {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private static final int GREEN = 0XFF00FF00;
    private static final int RED = 0xFFFF0000;
    private static final int BLUE = 0xFF0000FF;

    private static final BufferedImage ONE_PIXEL = PngEncoderBufferedImageConverter.createFromIntArgb(
            new int[1], 1, 1);

    @Test
    public void testEncode() {
        byte[] bytes = new PngEncoder()
                .withBufferedImage(ONE_PIXEL)
                .withCompressionLevel(1)
                .toBytes();

        int pngHeaderLength = PngEncoderLogic.FILE_BEGINNING.length;
        int ihdrLength = 25; // length(4)+"IHDR"(4)+values(13)+crc(4)
        int idatLength = 23; // length(4)+"IDAT"(4)+compressed scanline(11)+crc(4)
        int iendLength = PngEncoderLogic.FILE_ENDING.length;

        int expected = pngHeaderLength + ihdrLength + idatLength + iendLength;
        int actual = bytes.length;

        assertThat(actual, is(expected));
    }

    @Test
    public void testEncodeWithSrgb() {
        byte[] bytes = new PngEncoder()
                .withBufferedImage(ONE_PIXEL)
                .withCompressionLevel(1)
                .withSrgbRenderingIntent(PngEncoderSrgbRenderingIntent.PERCEPTUAL)
                .toBytes();

        int pngHeaderLength = PngEncoderLogic.FILE_BEGINNING.length;
        int ihdrLength = 25; // length(4)+"IHDR"(4)+values(13)+crc(4)
        int srgbLength = 13; // length(4)+"sRGB"(4)+value(1)+crc(4)
        int gamaLength = 16; // length(4)+"gAMA"(4)+value(4)+crc(4)
        int chrmLength = 44; // length(4)+"sRGB"(4)+value(32)+crc(4)
        int idatLength = 23; // length(4)+"IDAT"(4)+compressed scanline(11)+crc(4)
        int iendLength = PngEncoderLogic.FILE_ENDING.length;

        int expected = pngHeaderLength + ihdrLength + srgbLength + gamaLength + chrmLength + idatLength + iendLength;
        int actual = bytes.length;

        assertThat(actual, is(expected));
    }

    @Test
    public void testEncodeWithPhysicalPixelDimensions() {
        byte[] bytes = new PngEncoder()
                .withBufferedImage(ONE_PIXEL)
                .withCompressionLevel(1)
                .withPhysicalPixelDimensions(PngEncoderPhysicalPixelDimensions.dotsPerInch(300))
                .toBytes();

        int pngHeaderLength = PngEncoderLogic.FILE_BEGINNING.length;
        int ihdrLength = 25; // length(4)+"IHDR"(4)+values(13)+crc(4)
        int physLength = 21; // length(4)+"pHYs"(4)+values(9)+crc(4)
        int idatLength = 23; // length(4)+"IDAT"(4)+compressed scanline(11)+crc(4)
        int iendLength = PngEncoderLogic.FILE_ENDING.length;

        int expected = pngHeaderLength + ihdrLength + physLength + idatLength + iendLength;
        int actual = bytes.length;

        assertThat(actual, is(expected));
    }

    @Test
    public void testEncodeAndReadOpaque() throws IOException {
        int width = 3;
        int height = 2;
        int[] image = {
                WHITE, BLACK, RED,
                GREEN, WHITE, BLUE
        };
        BufferedImage bufferedImage = PngEncoderBufferedImageConverter.createFromIntArgb(
                image, width, height);
        byte[] bytes = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .toBytes();

        int[] actual = readWithImageIOgetRGB(bytes);
        int[] expected = image;
        assertThat(actual, is(expected));
    }

    @Test
    public void testEncodeAndReadBlackTransparency() throws IOException {
        int width = 0xFF;
        int height = 1;
        int[] image = new int[width];
        for (int x = 0; x < width; x++) {
            image[x] = x << 24;
        }
        BufferedImage bufferedImage = PngEncoderBufferedImageConverter.createFromIntArgb(
                image, width, height);
        byte[] bytes = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .toBytes();

        int[] actual = readWithImageIOgetRGB(bytes);
        int[] expected = image;
        assertThat(actual, is(expected));
    }

    @Test
    public void testEncodeAndReadRedTransparency() throws IOException {
        int width = 0xFF;
        int height = 1;
        int[] image = new int[width];
        for (int x = 0; x < width; x++) {
            int pixel = (x << 24) + (x << 16);
            image[x] = pixel;
        }
        BufferedImage bufferedImage = PngEncoderBufferedImageConverter.createFromIntArgb(
                image, width, height);
        byte[] bytes = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .toBytes();

        int[] actual = readWithImageIOgetRGB(bytes);
        int[] expected = image;
        assertThat(actual, is(expected));
    }

    @Test
    public void testEncodeAndReadRedTransparencyPredictor() throws IOException {
        int width = 0xFF;
        int height = 1;
        int[] image = new int[width];
        for (int x = 0; x < width; x++) {
            int pixel = (x << 24) + (x << 16);
            image[x] = pixel;
        }
        BufferedImage bufferedImage = PngEncoderBufferedImageConverter.createFromIntArgb(image, width, height);
        byte[] bytes = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .withPredictorEncoding(true)
                .toBytes();

        int[] actual = readWithImageIOgetRGB(bytes);
        int[] expected = image;
        assertThat(actual, is(expected));
    }

    @Test
    public void testPredictorEncoding() throws IOException {
        final BufferedImage bufferedImage = PngEncoderTestUtil
                .createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB);

        byte[] bytes = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .withMultiThreadedCompressionEnabled(false)
                .withPredictorEncoding(true)
                .toBytes();

        BufferedImage backReadImage = readWithImageIO(bytes);
        int[] actual = toIntArgb(backReadImage);
        int[] expected = toIntArgb(bufferedImage);
        assertThat(actual, is(expected));
    }

    @Test
    public void testPredictorEncodingWithImage() throws IOException {
        final BufferedImage bufferedImage = ImageIO
                .read(Objects.requireNonNull(PngEncoderTest.class.getResourceAsStream("/png-encoder-logo.png")));

        byte[] bytes = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .withPredictorEncoding(true)
                .toBytes();

        BufferedImage backReadImage = readWithImageIO(bytes);
        int[] actual = toIntArgb(backReadImage);
        int[] expected = toIntArgb(bufferedImage);
        assertThat(actual, is(expected));
    }

    @Test
    public void testpredictorEncodingCompareSize() throws IOException {
        final BufferedImage bufferedImage = ImageIO
                .read(Objects.requireNonNull(PngEncoderTest.class.getResourceAsStream("/png-encoder-logo.png")));

        byte[] bytesPred1 = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .withPredictorEncoding(true)
                .toBytes();
        byte[] bytesPred9 = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(9)
                .withPredictorEncoding(true)
                .toBytes();
        byte[] bytesBaseline1 = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .toBytes();
        byte[] bytesBaseline9 = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(9)
                .toBytes();

        System.out.println("Baseline 1: " + bytesBaseline1.length);
        System.out.println("Baseline 9: " + bytesBaseline9.length);
        System.out.println("Preditor 1: " + bytesPred1.length);
        System.out.println("Preditor 9: " + bytesPred9.length);
        assertThat("Predictor must be smaller", bytesPred1.length < bytesBaseline1.length);
        assertThat("Predictor must be smaller", bytesPred9.length < bytesBaseline9.length);
    }

    @Test
    public void testEncodeWithSrgbAndReadMetadata() throws IOException {
        int width = 3;
        int height = 2;
        int[] image = {
                WHITE, BLACK, RED,
                GREEN, WHITE, BLUE
        };
        BufferedImage bufferedImage = PngEncoderBufferedImageConverter.createFromIntArgb(
                image, width, height);
        byte[] bytes = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1)
                .withSrgbRenderingIntent(PngEncoderSrgbRenderingIntent.PERCEPTUAL)
                .toBytes();

        IIOMetadata metadata = getImageMetaDataWithImageIO(bytes);
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

        assertThat(root.getElementsByTagName("sRGB").getLength(), is(1));
        assertThat(root.getElementsByTagName("cHRM").getLength(), is(1));
        assertThat(root.getElementsByTagName("gAMA").getLength(), is(1));
    }

    @Test
    public void testEncodeWithPhysicalPixelDimensionsAndReadMetadata() throws IOException {
        int dotsPerInch = 150;
        byte[] bytes = new PngEncoder()
                .withBufferedImage(ONE_PIXEL)
                .withCompressionLevel(1)
                .withPhysicalPixelDimensions(PngEncoderPhysicalPixelDimensions.dotsPerInch(dotsPerInch))
                .toBytes();

        IIOMetadata metadata = getImageMetaDataWithImageIO(bytes);
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree("javax_imageio_1.0");
        float horizontalPixelSize = Float.parseFloat(((Element) root.getElementsByTagName("HorizontalPixelSize").item(0)).getAttribute("value"));
        float verticalPixelSize = Float.parseFloat(((Element) root.getElementsByTagName("VerticalPixelSize").item(0)).getAttribute("value"));

        // Standard metadata contains the width/height of a pixel in millimeters
        float mmPerInch = 25.4f;
        assertThat(Math.round(mmPerInch/horizontalPixelSize), is(dotsPerInch));
        assertThat(Math.round(mmPerInch/verticalPixelSize), is(dotsPerInch));
    }

    private static int[] readWithImageIOgetRGB(byte[] fileBytes) throws IOException {
        BufferedImage bufferedImage = readWithImageIO(fileBytes);

        return toIntArgb(bufferedImage);
    }

    private static int[] toIntArgb(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] argbImage = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                argbImage[y * width + x] = bufferedImage.getRGB(x, y);
            }
        }

        return argbImage;
    }

    public static BufferedImage readWithImageIO(byte[] filesBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(filesBytes)) {
            return ImageIO.read(bais);
        }
    }

    public static IIOMetadata getImageMetaDataWithImageIO(byte[] filesBytes) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(filesBytes);
                ImageInputStream input = ImageIO.createImageInputStream(inputStream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            ImageReader reader = readers.next();

            reader.setInput(input);
            return reader.getImageMetadata(0);
        }
    }

    @ParameterizedTest()
    @MethodSource("validCompressionLevels")
    public void validCompressionLevel(int compressionLevel) {
        assertDoesNotThrow(() -> new PngEncoder().withCompressionLevel(compressionLevel));
    }

    static IntStream validCompressionLevels() {
        // Compression level value must be between -1 and 9 inclusive.
        return IntStream.rangeClosed(-1, 9);
    }

    @ParameterizedTest()
    @ValueSource(ints = {-2, 10})
    public void invalidCompressionLevel(int compressionLevel) {
        assertThrows(IllegalArgumentException.class, () -> new PngEncoder().withCompressionLevel(compressionLevel));
    }

    @Test
    public void testEncodeWithoutImage() {
        // Document the fact that, at the moment, attempting to encode without providing an
        // image throws NullPointException.
        PngEncoder emptyEncoder = new PngEncoder();
        assertThrows(NullPointerException.class, () -> emptyEncoder.toBytes());

        PngEncoder encoderWithoutImage = new PngEncoder()
                .withCompressionLevel(9)
                .withMultiThreadedCompressionEnabled(true);
        assertThrows(NullPointerException.class, () -> encoderWithoutImage.toBytes());
    }
}
