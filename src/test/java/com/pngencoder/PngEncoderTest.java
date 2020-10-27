package com.pngencoder;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.zip.CRC32;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PngEncoderTest {
    public static final String VALID_CHUNK_TYPE = "IDAT";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private static final int GREEN = 0XFF00FF00;
    private static final int RED = 0xFFFF0000;
    private static final int BLUE = 0xFF0000FF;

    @Test
    public void testEncode() throws IOException {
        byte[] bytes = encodeToBytesIntArgb(new int[1], 1, 1);

        int pngHeaderLength = PngEncoderLogic.FILE_BEGINNING.length;
        int ihdrLength = 25; // length(4)+"IHDR"(4)+values(13)+crc(4)
        int idatLength = 23; // length(4)+"IDAT"(4)+compressed scanline(11)+crc(4)
        int iendLength = PngEncoderLogic.FILE_ENDING.length;

        int expected = pngHeaderLength + ihdrLength + idatLength + iendLength;
        int actual = bytes.length;

        assertThat(actual, is(expected));
    }

    @Test
    public void testEncodeWithSrgb() throws IOException {
        byte[] bytes = encodeToBytesIntArgb(new int[1], 1, 1, true);

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

        byte[] bytes = encodeToBytesIntArgb(image, width, height);

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

        byte[] bytes = encodeToBytesIntArgb(image, width, height);

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

        byte[] bytes = encodeToBytesIntArgb(image, width, height);

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

        byte[] bytes = encodeToBytesIntArgb(image, width, height, true);

        IIOMetadata metadata = getImageMetaDataWithImageIO(bytes);
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());

        assertThat(root.getElementsByTagName("sRGB").getLength(), is(1));
        assertThat(root.getElementsByTagName("cHRM").getLength(), is(1));
        assertThat(root.getElementsByTagName("gAMA").getLength(), is(1));
    }

    private static byte[] encodeToBytesIntArgb(int[] image, int width, int height) {
        return encodeToBytesIntArgb(image, width, height, false);
    }

    private static byte[] encodeToBytesIntArgb(int[] image, int width, int height, boolean addSrgbChunk) {
        BufferedImage bufferedImage = PngEncoderBufferedImageConverter.createFromIntArgb(image, width, height);
        PngEncoder pngEncoder = new PngEncoder()
                .withBufferedImage(bufferedImage)
                .withCompressionLevel(1);
        if (addSrgbChunk) {
            pngEncoder = pngEncoder.withSrgbRenderingIntent(PngEncoderSrgbRenderingIntent.PERCEPTUAL);
        }
        return pngEncoder.toBytes();
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

    @Test(expected = NullPointerException.class)
    public void testAsChunkTypeNull() {
        PngEncoderLogic.asChunk(null, new byte[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsChunkTypeInvalid() {
        PngEncoderLogic.asChunk("chunk types must be four characters long", new byte[1]);
    }

    @Test(expected = NullPointerException.class)
    public void testAsChunkNullBytes() {
        PngEncoderLogic.asChunk(VALID_CHUNK_TYPE, null);
    }

    @Test
    public void testAsChunkAdds12Bytes() {
        byte[] data = {5};
        byte[] chunk = PngEncoderLogic.asChunk(VALID_CHUNK_TYPE, data);

        int expected = 13;
        int actual = chunk.length;
        assertThat(actual, is(expected));
    }

    @Test
    public void testAsChunkFirstByteIsSizeOfData() {
        byte[] data = {5, 7};
        byte[] chunk = PngEncoderLogic.asChunk(VALID_CHUNK_TYPE, data);

        byte[] expected = intToBytes(data.length);
        assertThat(chunk[0], is(expected[0]));
        assertThat(chunk[1], is(expected[1]));
        assertThat(chunk[2], is(expected[2]));
        assertThat(chunk[3], is(expected[3]));
    }

    @Test
    public void testAsChunkCrcIsCalculatedFromTypeAndData() {
        byte[] data = {5, 7};
        byte[] chunk = PngEncoderLogic.asChunk(VALID_CHUNK_TYPE, data);

        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.mark();
        byteBuffer.put(VALID_CHUNK_TYPE.getBytes());
        byteBuffer.put(data);
        byteBuffer.reset();
        int expectedCrc = PngEncoderLogic.getCrc32(byteBuffer);

        byte[] expected = intToBytes(expectedCrc);
        assertThat(chunk[10], is(expected[0]));
        assertThat(chunk[11], is(expected[1]));
        assertThat(chunk[12], is(expected[2]));
        assertThat(chunk[13], is(expected[3]));
    }

    @Test
    public void testCrcWithPartialByteBuffer() {
        byte[] b = {5};

        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        byteBuffer.put("garbage".getBytes());

        byteBuffer.mark();
        ByteBuffer slice = byteBuffer.slice().asReadOnlyBuffer();
        byteBuffer.put(b);
        slice.limit(b.length);
        byteBuffer.put("more garbage".getBytes());

        int actual = PngEncoderLogic.getCrc32(slice);
        int expected = getSimpleCrc(b);
        assertThat(actual, is(expected));
    }

    private static int getSimpleCrc(byte[] b) {
        CRC32 crc32 = new CRC32();
        crc32.update(b);
        return (int) crc32.getValue();
    }

    private static byte[] intToBytes(int val) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(val);
        return byteBuffer.array();
    }
}
