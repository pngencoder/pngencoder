package com.pngencoder;

import org.junit.Ignore;
import org.junit.Test;
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
 * PngEncoderBenchmarkPngEncoderVsImageIO.random1024x1024ImageIO      thrpt         5.204          ops/s
 * PngEncoderBenchmarkPngEncoderVsImageIO.random1024x1024PngEncoder   thrpt        31.450          ops/s
 * 31.450 / 5.204 = 6.0 times faster
 *
 * PngEncoderBenchmarkPngEncoderVsImageIO.logo2121x350ImageIO         thrpt        25.937          ops/s
 * PngEncoderBenchmarkPngEncoderVsImageIO.logo2121x350PngEncoder      thrpt       135.373          ops/s
 * 135.373 / 25.937 = 5.2 times faster
 *
 * PngEncoderBenchmarkPngEncoderVsImageIO.looklet4900x6000ImageIO     thrpt         0.029          ops/s
 * PngEncoderBenchmarkPngEncoderVsImageIO.looklet4900x6000PngEncoder  thrpt         0.161          ops/s
 * 0.161 / 0.029 = 5.6 times faster
 */
public class PngEncoderBenchmarkPngEncoderVsImageIO {
    private static final Options OPTIONS = new OptionsBuilder()
            .include(PngEncoderBenchmarkPngEncoderVsImageIO.class.getSimpleName() + ".*")
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

    @Ignore("run manually")
    @Test
    public void runBenchmark() throws Exception {
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
