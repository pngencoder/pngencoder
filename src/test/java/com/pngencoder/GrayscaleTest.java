package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GrayscaleTest {
    @Test
    void testGrayscaleGifShouldBeSavedProperly() throws IOException {

        final BufferedImage bufferedImage = PngEncoderTestUtil.readTestImageResource("thermos_36667_sm.gif");

        byte[] png = new PngEncoder().withBufferedImage(bufferedImage)
                .toBytes();

        new PngEncoder().withBufferedImage(bufferedImage)
                .toFile("out.png");

        BufferedImage unpacked = ImageIO.read(new ByteArrayInputStream(png));

//        PngEncoderTestUtil.encodeWithImageIO(bufferedImage);

        PngEncoderTestUtil.assertThatImageIsEqual(unpacked, bufferedImage);
    }

    @Test
    void testGrayscaleSynthetic() throws IOException {
        BufferedImage bufferedImage = new BufferedImage(1, 2, 13);
        Graphics g = bufferedImage.getGraphics();

        g.setColor(new Color(4, 4, 4));
        g.drawRect(0, 0, 1, 1);

        g.setColor(new Color(255, 255, 255));
        g.drawRect(0, 0, 1, 1);


        byte[] png = new PngEncoder().withBufferedImage(bufferedImage)
                .toBytes();

        new PngEncoder().withBufferedImage(bufferedImage)
                .toFile("out.png");

        BufferedImage unpacked = ImageIO.read(new ByteArrayInputStream(png));

        //        PngEncoderTestUtil.encodeWithImageIO(bufferedImage);
        PngEncoderTestUtil.assertThatImageIsEqual(unpacked, bufferedImage);
    }
}
