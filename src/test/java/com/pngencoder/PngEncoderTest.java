package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

    private static int[] readWithImageIOgetRGB(byte[] fileBytes) throws IOException {
        BufferedImage bufferedImage = readWithImageIO(fileBytes);

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] argbImage = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                argbImage[y*width + x] = bufferedImage.getRGB(x, y);
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
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(filesBytes); ImageInputStream input = ImageIO.createImageInputStream(inputStream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            ImageReader reader = readers.next();

            reader.setInput(input);
            return reader.getImageMetadata(0);
        }
    }
}
