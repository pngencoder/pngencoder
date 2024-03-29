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

/**
 * Benchmark                                                           Mode  Cnt    Score   Error  Units
 *
 * PngEncoderBenchmarkPngEncoderVsImageIO.random1024x1024ImageIO      thrpt         5.150          ops/s
 * PngEncoderBenchmarkPngEncoderVsImageIO.random1024x1024PngEncoder   thrpt        36.324          ops/s
 * 36.324 / 5.150 = 7.1 times faster
 *
 * PngEncoderBenchmarkPngEncoderVsImageIO.logo2121x350ImageIO         thrpt        24.857          ops/s
 * PngEncoderBenchmarkPngEncoderVsImageIO.logo2121x350PngEncoder      thrpt       127.034          ops/s
 * 127.034 / 24.857 = 5.1 times faster
 *
 * PngEncoderBenchmarkPngEncoderVsImageIO.looklet4900x6000ImageIO     thrpt         0.029          ops/s
 * PngEncoderBenchmarkPngEncoderVsImageIO.looklet4900x6000PngEncoder  thrpt         0.159          ops/s
 * 0.159 / 0.029 = 5.5 times faster
 */
public class PngEncoderBenchmarkPngEncoderVsImageIO {

    private static Options options(int threads) {
        return new OptionsBuilder()
                .include(PngEncoderBenchmarkPngEncoderVsImageIO.class.getSimpleName() + ".*")
                .shouldFailOnError(true)
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .threads(threads)
                .forks(1)
                .warmupIterations(1)
                .measurementIterations(1)
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
    public static class BenchmarkStateLooklet4900x6000 {
        final BufferedImage bufferedImage = PngEncoderBufferedImageConverter.ensureType(PngEncoderTestUtil.readTestImageResource("looklet-look-scale6.png"), PngEncoderBufferedImageType.TYPE_INT_ARGB);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkStateLogo2121x350 {
        final BufferedImage bufferedImage = PngEncoderTestUtil.readTestImageResource("png-encoder-logo.png");
    }

    @Benchmark
    public void random1024x1024PngEncoder(BenchmarkStateRandom1024x1024 state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage);
    }

    @Benchmark
    public void looklet4900x6000PngEncoder(BenchmarkStateLooklet4900x6000 state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage);
    }

    @Benchmark
    public void logo2121x350PngEncoder(BenchmarkStateLogo2121x350 state) {
        PngEncoderTestUtil.encodeWithPngEncoder(state.bufferedImage);
    }

    @Benchmark
    public void random1024x1024ImageIO(BenchmarkStateRandom1024x1024 state) {
        PngEncoderTestUtil.encodeWithImageIO(state.bufferedImage);
    }

    @Benchmark
    public void looklet4900x6000ImageIO(BenchmarkStateLooklet4900x6000 state) {
        PngEncoderTestUtil.encodeWithImageIO(state.bufferedImage);
    }

    @Benchmark
    public void logo2121x350ImageIO(BenchmarkStateLogo2121x350 state) {
        PngEncoderTestUtil.encodeWithImageIO(state.bufferedImage);
    }
}
