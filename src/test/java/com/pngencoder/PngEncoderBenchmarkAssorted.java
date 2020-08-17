package com.pngencoder;

import org.junit.Ignore;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PngEncoderBenchmarkAssorted {
    @Ignore("run manually")
    @Test
    public void runBenchmarkIntelliJIdeaProfilerPngEncoder() {
        final int times = 10;
        final BufferedImage bufferedImage = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("looklet-look-scale6.png"), PngEncoderBufferedImageType.TYPE_INT_ARGB);
        for (int i = 0; i < times; i++) {
            PngEncoderTestUtil.encodeWithPngEncoder(bufferedImage);
        }
    }

    @Ignore("run manually")
    @Test
    public void runBenchmarkIntelliJIdeaProfilerImageIO() {
        final int times = 1;
        final BufferedImage bufferedImage = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("looklet-look-scale6.png"), PngEncoderBufferedImageType.TYPE_INT_ARGB);
        for (int i = 0; i < times; i++) {
            PngEncoderTestUtil.encodeWithImageIO(bufferedImage);
        }
    }

    @Ignore("run manually")
    @Test
    public void runBenchmarkCustom() throws IOException {
        Timing.message("started");

        //final BufferedImage original = readTestImageResource("png-encoder-logo.png");
        final BufferedImage original = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("looklet-look-scale6.png"), PngEncoderBufferedImageType.TYPE_INT_RGB);
        Timing.message("loaded");

        final File outImageIO = File.createTempFile("out-imageio", ".png");
        //final File outPngEncoder = File.createTempFile("out-pngencoder", ".png");
        final File outPngEncoder = new File("/Users/olof/Desktop/out.png");

        ImageIO.write(original, "png", outImageIO);
        Timing.message("ImageIO Warmup");

        ImageIO.write(original, "png", outImageIO);
        Timing.message("ImageIO Result");

        PngEncoder pngEncoder = new PngEncoder()
                //.withMultiThreadedCompressionEnabled(false)
                .withCompressionLevel(9)
                .withBufferedImage(original);
        System.out.println(outPngEncoder);

        pngEncoder.toFile(outPngEncoder);
        Timing.message("PngEncoder Warmup");

        pngEncoder.toFile(outPngEncoder);
        Timing.message("PngEncoder Result");

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
