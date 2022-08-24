package com.pngencoder;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PngEncoderBenchmarkCompressionSpeedVsSize {
    private static final Options OPTIONS = new OptionsBuilder()
            .include(PngEncoderBenchmarkCompressionSpeedVsSize.class.getSimpleName() + ".*")
            .shouldFailOnError(true)
            .mode(Mode.Throughput)
            .timeUnit(TimeUnit.SECONDS)
            .threads(1)
            .forks(1)
            .warmupIterations(2)
            .measurementIterations(1)
            .warmupTime(TimeValue.seconds(3))
            .measurementTime(TimeValue.seconds(7))
            .build();

    @Disabled("run manually")
    @Test
    public void runBenchmarkSpeed() throws Exception {
        new Runner(OPTIONS).run();
    }

    @Disabled("run manually")
    @Test
    public void runBenchmarkSize() {
        final BufferedImage bufferedImage = createTestImage();
        for (int compressionLevel = 0; compressionLevel <= 9; compressionLevel++) {
            final int fileSize = PngEncoderTestUtil.encodeWithPngEncoder(bufferedImage, compressionLevel);
            String message = String.format("compressionLevel: %d fileSize: %d", compressionLevel, fileSize);
            System.out.println(message);
        }

        for (int compressionLevel = 0; compressionLevel <= 9; compressionLevel++) {
            final int fileSize = PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(bufferedImage, compressionLevel);
            String message = String.format("compressionLevel (withPredictor): %d fileSize: %d", compressionLevel, fileSize);
            System.out.println(message);
        }
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        final BufferedImage bufferedImage = createTestImage();
    }

    private static Random random = new Random();
    @Benchmark
    public void loopVariableInline(Blackhole blackhole) {
        int height = 1000;
        int width = 1000;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                blackhole.consume(width*y + x);
            }
        }
    }

    @Benchmark
    public void loopVariablePtr(Blackhole blackhole) {
        int height = 1000;
        int width = 1000;

        int ptr = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                blackhole.consume(ptr++);
            }
        }
    }

    private static BufferedImage createTestImage() {
        return new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);
    }
}
