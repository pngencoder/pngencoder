package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * LICENCE:
 * thermos_36667_sm.gif was downloaded from
 * https://etc.usf.edu/clipart/36600/36667/thermos_36667_sm.gif
 * It is used as an example of an indexed input with no color depth.
 * The png encoder may (should?) convert it into a single grayscale channel.
 * <p>
 * https://etc.usf.edu/clipart/info/license
 */
public class PngEncoderInputTypesTest {

    @Test
    void testGrayscaleGifShouldBeSavedProperly() throws IOException {
        final BufferedImage bufferedImage = getRealGifImage();

        byte[] png = new PngEncoder().withBufferedImage(bufferedImage)
                .toBytes();

        BufferedImage unpacked = ImageIO.read(new ByteArrayInputStream(png));

        PngEncoderTestUtil.assertThatImageIsEqual(unpacked, bufferedImage);

        BufferedImage subimage = bufferedImage.getSubimage(10, 10, 50, 50);
        byte[] pngSubimage = new PngEncoder().withBufferedImage(subimage)
                .toBytes();

        BufferedImage unpackedSubimage = ImageIO.read(new ByteArrayInputStream(pngSubimage));
        PngEncoderTestUtil.assertThatImageIsEqual(subimage, unpackedSubimage);
    }

    static BufferedImage getRealGifImage() {
        return PngEncoderTestUtil.readTestImageResource("thermos_36667_sm.gif");
    }

    @Test
    void showcaseMismatchInNumberOfChannels() {
        final BufferedImage image = getRealGifImage();

        final int channels = image.getRaster().getSampleModel().getNumBands();

        PngEncoderScanlineUtil.EncodingMetaInfo metaInfo = PngEncoderScanlineUtil.getEncodingMetaInfo(image);

        assertEquals(channels, 1);
        assertEquals(metaInfo.channels, 3);
        // By default, we will fall back to encode the image as sRGB image
        assertEquals(metaInfo.colorSpaceType, PngEncoderScanlineUtil.EncodingMetaInfo.ColorSpaceType.Rgb);
    }

    @Test
    void testAllTypesOfInput() throws IOException {

        for (int i = 1; i <= 13; i++) {
            BufferedImage bufferedImage = new BufferedImage(1, 2, i);

            Graphics g = bufferedImage.getGraphics();

            g.setColor(new Color(4, 4, 4));
            g.drawRect(0, 0, 1, 1);

            g.setColor(new Color(255, 255, 255));
            g.drawRect(0, 1, 1, 1);

            byte[] png = new PngEncoder().withBufferedImage(bufferedImage)
                    .toBytes();

            BufferedImage unpacked = ImageIO.read(new ByteArrayInputStream(png));

            PngEncoderTestUtil.assertThatImageIsEqual(unpacked, bufferedImage);
        }
    }

    @Test
    void testGrayscaleIndexed() throws IOException {
        BufferedImage bufferedImage = new BufferedImage(1, 2, BufferedImage.TYPE_BYTE_INDEXED);
        Graphics g = bufferedImage.getGraphics();

        g.setColor(new Color(4, 4, 4));
        g.drawRect(0, 0, 1, 1);

        g.setColor(new Color(255, 255, 255));
        g.drawRect(0, 1, 1, 1);

        byte[] png = new PngEncoder().withBufferedImage(bufferedImage)
                .toBytes();

        BufferedImage unpacked = ImageIO.read(new ByteArrayInputStream(png));

        PngEncoderTestUtil.assertThatImageIsEqual(unpacked, bufferedImage);
    }

    @Test
    void testColorIndexed() throws IOException {
        BufferedImage bufferedImage = new BufferedImage(1, 2, BufferedImage.TYPE_BYTE_INDEXED);
        Graphics g = bufferedImage.getGraphics();

        g.setColor(new Color(4, 4, 4));
        g.drawRect(0, 0, 1, 1);

        g.setColor(new Color(255, 0, 0));
        g.drawRect(0, 1, 1, 1);

        byte[] png = new PngEncoder().withBufferedImage(bufferedImage)
                .toBytes();

        BufferedImage unpacked = ImageIO.read(new ByteArrayInputStream(png));

        PngEncoderTestUtil.assertThatImageIsEqual(unpacked, bufferedImage);
    }
}
