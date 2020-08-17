package com.pngencoder;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.zip.Deflater;

class PngEncoderDeflaterSegmentTask implements Supplier<PngEncoderDeflaterSegmentResult> {
    private final PngEncoderDeflaterBuffer previousOriginalSegment;
    private final PngEncoderDeflaterBuffer originalSegment;
    private final PngEncoderDeflaterBuffer deflatedSegment;
    private final int compressionLevel;
    private final boolean lastSegment;

    public PngEncoderDeflaterSegmentTask(
            PngEncoderDeflaterBuffer previousOriginalSegment,
            PngEncoderDeflaterBuffer originalSegment,
            PngEncoderDeflaterBuffer deflatedSegment,
            int compressionLevel,
            boolean lastSegment) {
        this.previousOriginalSegment = Objects.requireNonNull(previousOriginalSegment, "previousOriginalSegment");
        this.originalSegment = Objects.requireNonNull(originalSegment, "originalSegment");
        this.deflatedSegment = Objects.requireNonNull(deflatedSegment, "deflatedSegment");
        this.compressionLevel = compressionLevel;
        this.lastSegment = lastSegment;
    }

    @Override
    public PngEncoderDeflaterSegmentResult get() {
        final long originalSegmentAdler32 = originalSegment.calculateAdler32();
        final int originalSegmentLength = originalSegment.length;

        deflate(previousOriginalSegment, originalSegment, deflatedSegment, compressionLevel, lastSegment);

        return new PngEncoderDeflaterSegmentResult(previousOriginalSegment, deflatedSegment, originalSegmentAdler32, originalSegmentLength);
    }

    static void deflate(PngEncoderDeflaterBuffer previousOriginalSegment, PngEncoderDeflaterBuffer originalSegment, PngEncoderDeflaterBuffer deflatedSegment, int compressionLevel, boolean lastSegment) {
        final Deflater deflater = PngEncoderDeflaterThreadLocalDeflater.getInstance(compressionLevel);
        deflater.setInput(originalSegment.bytes, 0, originalSegment.length);

        // We only set the previous "dictionary" bytes if best compression is requested.
        // It slows down speed considerably due to reduced ability to parallelize.
        if (previousOriginalSegment.length > 0 && compressionLevel == Deflater.BEST_COMPRESSION) {
            final int dictionaryLength = Math.min(PngEncoderLogic.SEGMENT_MAX_LENGTH_DICTIONARY, previousOriginalSegment.length);
            final int dictionaryOffset = previousOriginalSegment.length - dictionaryLength;
            deflater.setDictionary(previousOriginalSegment.bytes, dictionaryOffset, dictionaryLength);
        }

        if (lastSegment) {
            deflater.finish();
        }

        deflatedSegment.length = deflater.deflate(deflatedSegment.bytes, 0, deflatedSegment.bytes.length, lastSegment ? Deflater.NO_FLUSH : Deflater.SYNC_FLUSH);
    }
}
