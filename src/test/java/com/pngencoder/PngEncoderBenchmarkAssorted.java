package com.pngencoder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

    private static final Options OPTIONS = new OptionsBuilder()
            .include(PngEncoderBenchmarkAssorted.class.getSimpleName() + ".*")
            .shouldFailOnError(true)
            .mode(Mode.Throughput)
            .timeUnit(TimeUnit.SECONDS)
            .threads(1)
            .forks(1)
            .warmupIterations(1)
            .measurementIterations(1)
            .warmupTime(TimeValue.seconds(2))
            .measurementTime(TimeValue.seconds(10))
            .build();

    @Disabled("run manually")
    @Test
    public void runBenchmarkCustom() throws Exception {
        new Runner(OPTIONS).run();
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateRandom1024x1024 {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB, 1024);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateLooklet4900x6000 {
        final BufferedImage bufferedImage = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("looklet-look-scale6.png"), PngEncoderBufferedImageType.TYPE_INT_ARGB);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateLogo2121x350 {
        final BufferedImage bufferedImage = PngEncoderTestUtil.readTestImageResource("png-encoder-logo.png");
    }

    @Benchmark
    public void benchmarkCustom1024(BenchmarkStateRandom1024x1024 state) throws IOException {
        doCustomBenchmarkOperations(state.bufferedImage);
    }

    @Benchmark
    public void benchmarkCustomLooklet(BenchmarkStateLooklet4900x6000 state) throws IOException {
        doCustomBenchmarkOperations(state.bufferedImage);
    }

    @Benchmark
    public void benchmarkCustomLogo(BenchmarkStateLogo2121x350 state) throws IOException {
        doCustomBenchmarkOperations(state.bufferedImage);
    }

    private void doCustomBenchmarkOperations(BufferedImage original) throws IOException {
        final File outImageIO = File.createTempFile("out-imageio", ".png");
        final File outPngEncoder = new File("target/test/assorted_out.png");
//        System.out.println(outPngEncoder.getAbsolutePath());
        outPngEncoder.getParentFile().mkdir();

        PngEncoder pngEncoder = new PngEncoder()
                .withMultiThreadedCompressionEnabled(true)
                //.withPredictorEncoding(true)
                .withCompressionLevel(4)
                .withBufferedImage(original);
//        System.out.println("saving " + outPngEncoder + "...");
        pngEncoder.toFile(outPngEncoder);
        /*
        System.out.println("saved");

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
        */
    }
}
