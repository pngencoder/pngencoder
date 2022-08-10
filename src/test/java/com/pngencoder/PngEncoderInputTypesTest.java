package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * LICENCE:
 * thermos_36667_sm.gif was downloaded from
 * https://etc.usf.edu/clipart/36600/36667/thermos_36667_sm.gif
 * It is used as an example of an indexed input with no color depth.
 * The png encoder may (should?) convert it into a single grayscale channel.
 *
 * https://etc.usf.edu/clipart/info/license
 */
public class PngEncoderInputTypesTest {

    @Test
    void testWorkaround() throws IOException {
        BufferedImage bufferedImage = PngEncoderTestUtil.readTestImageResource("thermos_36667_sm.gif");

        if (bufferedImage.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            final BufferedImage bgr = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            bgr.getGraphics().drawImage(bufferedImage, 0, 0, null);
            bufferedImage = bgr;
        }

        byte[] png = new PngEncoder().withBufferedImage(bufferedImage)
                .toBytes();

        BufferedImage unpacked = ImageIO.read(new ByteArrayInputStream(png));

        PngEncoderTestUtil.assertThatImageIsEqual(unpacked, bufferedImage);
    }

    @Test
    void testGrayscaleGifShouldBeSavedProperly() throws IOException {
        final BufferedImage bufferedImage = getRealGifImage();

        byte[] png = new PngEncoder().withBufferedImage(bufferedImage)
                .toBytes();

        BufferedImage unpacked = ImageIO.read(new ByteArrayInputStream(png));

        PngEncoderTestUtil.assertThatImageIsEqual(unpacked, bufferedImage);
    }

    private BufferedImage getRealGifImage() {
        return PngEncoderTestUtil.readTestImageResource("thermos_36667_sm.gif");
    }

    @Test
    void showcaseMismatchInNumberOfChannels() {
        final BufferedImage image = getRealGifImage();

        final int channels = image.getRaster().getSampleModel().getNumBands();

        PngEncoderScanlineUtil.EncodingMetaInfo metaInfo = PngEncoderScanlineUtil.getEncodingMetaInfo(image);

        assertEquals(channels, 1);
        assertEquals(metaInfo.channels, 3);
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
        BufferedImage bufferedImage = new BufferedImage(1, 2, 13);
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
        BufferedImage bufferedImage = new BufferedImage(1, 2, 13);
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
