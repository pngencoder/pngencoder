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

public class PngEncoderBenchmarkPreMultipliedAlpha {

    private static Options options(int threads) {
        return new OptionsBuilder()
                .include(PngEncoderBenchmarkPreMultipliedAlpha.class.getSimpleName() + ".*")
                .shouldFailOnError(true)
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .threads(threads)
                .forks(1)
                .warmupIterations(2)
                .measurementIterations(3)
                .warmupTime(TimeValue.seconds(2))
                .measurementTime(TimeValue.seconds(5))
                .build();
    }

    @Disabled("run manually")
    @Test
    public void runBenchmarkOneThread() throws Exception {
        new Runner(options(1)).run();
    }

    @Disabled("run manually")
    @Test
    public void runBenchmarkEightThreads() throws Exception {
        new Runner(options(8)).run();
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateRandom1024x1024 {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB, 1024);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateRandom1024x1024PreMultipliedAlpha {
        final BufferedImage bufferedImage = PngEncoderTestUtil.createTestImage(PngEncoderBufferedImageType.TYPE_INT_ARGB_PRE, 1024);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateLogo2121x350 {
        final BufferedImage bufferedImage = PngEncoderTestUtil.readTestImageResource("png-encoder-logo.png");
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateLogo2121x350PreMultipliedAlpha {
        final BufferedImage bufferedImage = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("png-encoder-logo.png"), PngEncoderBufferedImageType.TYPE_INT_ARGB_PRE);
    }

    @Benchmark
    public void random1024x1024PngEncoder(BenchmarkStateRandom1024x1024 state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage);
    }

    @Benchmark
    public void random1024x1024PreMultipliedAlphaPngEncoder(BenchmarkStateRandom1024x1024PreMultipliedAlpha state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage);
    }

    @Benchmark
    public void random1024x1024PreMultipliedAlphaPngEncoderHack(BenchmarkStateRandom1024x1024PreMultipliedAlpha state) {
        PngEncoderTestUtil.encodeWithPngEncoder(PngEncoderBufferedImageConverter.ensureType(state.bufferedImage, PngEncoderBufferedImageType.TYPE_4BYTE_ABGR));
    }

    @Benchmark
    public void random1024x1024ImageIO(BenchmarkStateRandom1024x1024 state) {
        PngEncoderTestUtil.encodeWithImageIO(state.bufferedImage);
    }

    @Benchmark
    public void random1024x1024PreMultipliedAlphaImageIO(BenchmarkStateRandom1024x1024PreMultipliedAlpha state) {
        PngEncoderTestUtil.encodeWithImageIO(state.bufferedImage);
    }

    @Benchmark
    public void logo2121x350PngEncoder(BenchmarkStateLogo2121x350 state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage);
    }

    @Benchmark
    public void logo2121x350PreMultipliedAlphaPngEncoder(BenchmarkStateLogo2121x350PreMultipliedAlpha state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage);
    }

    @Benchmark
    public void logo2121x350PreMultipliedAlphaPngEncoderHack(BenchmarkStateLogo2121x350PreMultipliedAlpha state) {
        PngEncoderTestUtil.encodeWithPngEncoder(PngEncoderBufferedImageConverter.ensureType(state.bufferedImage, PngEncoderBufferedImageType.TYPE_4BYTE_ABGR));
    }

    @Benchmark
    public void logo2121x350PreMultipliedAlphaImageIO(BenchmarkStateLogo2121x350PreMultipliedAlpha state) {
        PngEncoderTestUtil.encodeWithImageIO(state.bufferedImage);
    }

    @Benchmark
    public void logo2121x350ImageIO(BenchmarkStateLogo2121x350 state) {
        PngEncoderTestUtil.encodeWithImageIO(state.bufferedImage);
    }
}
