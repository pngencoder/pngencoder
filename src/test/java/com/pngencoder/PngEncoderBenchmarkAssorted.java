package com.pngencoder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PngEncoderBenchmarkAssorted {
    @Disabled("run manually")
    @Test
    public void runBenchmarkIntelliJIdeaProfilerPngEncoder() {
        final int times = 10;
        final BufferedImage bufferedImage = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("looklet-look-scale6.png"), PngEncoderBufferedImageType.TYPE_INT_ARGB);
        for (int i = 0; i < times; i++) {
            PngEncoderTestUtil.encodeWithPngEncoder(bufferedImage);
        }
    }

    @Disabled("run manually")
    @Test
    public void runBenchmarkIntelliJIdeaProfilerImageIO() {
        final int times = 1;
        final BufferedImage bufferedImage = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("looklet-look-scale6.png"), PngEncoderBufferedImageType.TYPE_INT_ARGB);
        for (int i = 0; i < times; i++) {
            PngEncoderTestUtil.encodeWithImageIO(bufferedImage);
        }
    }

    private final static boolean DISALBE_IMAGE_IO = false;

    @Disabled("run manually")
    @Test
    public void runBenchmarkCustom() throws IOException {
        Timing.message("started");

        //final BufferedImage original = readTestImageResource("png-encoder-logo.png");
        final BufferedImage original = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("looklet-look-scale6.png"), PngEncoderBufferedImageType.TYPE_INT_RGB);
        Timing.message("loaded");

        final File outImageIO = File.createTempFile("out-imageio", ".png");
        //final File outPngEncoder = File.createTempFile("out-pngencoder", ".png");
        final File outPngEncoder = new File("target/test/assorted_out.png");
        System.out.println(outPngEncoder.getAbsolutePath());
        outPngEncoder.getParentFile().mkdir();

        if (!DISALBE_IMAGE_IO) {
            ImageIO.write(original, "png", outImageIO);
            Timing.message("ImageIO Warmup");

            ImageIO.write(original, "png", outImageIO);
            Timing.message("ImageIO Result");
        }

        PngEncoder pngEncoder = new PngEncoder()
                .withMultiThreadedCompressionEnabled(true)
                //.withPredictorEncoding(true)
                .withCompressionLevel(4)
                .withBufferedImage(original);
        System.out.println(outPngEncoder);

        pngEncoder.toFile(outPngEncoder);
        Timing.message("PngEncoder Warmup");

        for (int i = 0; i < 10; i++) {
            pngEncoder.toFile(outPngEncoder);
            Timing.message("PngEncoder Result " + i);
        }

        final long imageIOSize = outImageIO.length();
        final long pngEncoderSize = outPngEncoder.length();

        if (imageIOSize != 0) {
            System.out.println("imageIOSize: " + imageIOSize);
        }

        if (pngEncoderSize != 0) {
            System.out.println("pngEncoderSize: " + pngEncoderSize);
        }

        if (imageIOSize != 0 && pngEncoderSize != 0) {
            System.out.println("pngEncoderSize / imageIOSize: " + (double) pngEncoderSize / (double) imageIOSize);
        }
    }
}
