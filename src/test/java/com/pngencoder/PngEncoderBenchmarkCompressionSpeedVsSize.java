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
import java.util.concurrent.TimeUnit;

public class PngEncoderBenchmarkCompressionSpeedVsSize {
    private static final Options OPTIONS = new OptionsBuilder()
            .include(PngEncoderBenchmarkCompressionSpeedVsSize.class.getSimpleName() + ".*")
            .shouldFailOnError(true)
            .mode(Mode.Throughput)
            .timeUnit(TimeUnit.SECONDS)
            .threads(1)
            .forks(1)
            .warmupIterations(1)
            .measurementIterations(1)
            .warmupTime(TimeValue.seconds(2))
            .measurementTime(TimeValue.seconds(5))
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
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        final BufferedImage bufferedImage = createTestImage();
    }

    @Benchmark
    public void compressionLevel0(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 0);
    }

    @Benchmark
    public void compressionLevel1(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 1);
    }

    @Benchmark
    public void compressionLevel2(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 2);
    }

    @Benchmark
    public void compressionLevel3(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 3);
    }

    @Benchmark
    public void compressionLevel4(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 4);
    }

    @Benchmark
    public void compressionLevel5(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 5);
    }

    @Benchmark
    public void compressionLevel6(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 6);
    }

    @Benchmark
    public void compressionLevel7(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 7);
    }

    @Benchmark
    public void compressionLevel8(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 8);
    }

    @Benchmark
    public void compressionLevel9(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage, 9);
    }

    @Benchmark
    public void compressionLevel0Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 0);
    }

    @Benchmark
    public void compressionLevel1Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 1);
    }

    @Benchmark
    public void compressionLevel2Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 2);
    }

    @Benchmark
    public void compressionLevel3Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 3);
    }

    @Benchmark
    public void compressionLevel4Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 4);
    }

    @Benchmark
    public void compressionLevel5Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 5);
    }

    @Benchmark
    public void compressionLevel6Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 6);
    }

    @Benchmark
    public void compressionLevel7Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 7);
    }

    @Benchmark
    public void compressionLevel8Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 8);
    }

    @Benchmark
    public void compressionLevel9Predictor(BenchmarkState state) {
        PngEncoderTestUtil.encodeWithPngEncoderPredictorEncoding(state.bufferedImage, 9);
    }

    private static BufferedImage createTestImage() {
        return PngEncoderTestUtil.readTestImageResource("png-encoder-logo.png");
    }
}
