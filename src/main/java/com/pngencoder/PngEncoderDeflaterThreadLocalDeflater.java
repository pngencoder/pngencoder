package com.pngencoder;

import java.util.zip.Deflater;

/**
 * We save time by allocating and reusing some thread local state.
 * <p>
 * Creating a new Deflater instance takes a surprising amount of time.
 * Resetting an existing Deflater instance is almost free though.
 */
class PngEncoderDeflaterThreadLocalDeflater {
    private static final ThreadLocal<PngEncoderDeflaterThreadLocalDeflater> THREAD_LOCAL = ThreadLocal.withInitial(PngEncoderDeflaterThreadLocalDeflater::new);

    static Deflater getInstance(int compressionLevel) {
        return THREAD_LOCAL.get().getDeflater(compressionLevel);
    }

    private final Deflater[] deflaters;

    private PngEncoderDeflaterThreadLocalDeflater() {
        this.deflaters = new Deflater[11];
    }

    private Deflater getDeflater(int compressionLevel) {
        Deflater deflater = this.deflaters[compressionLevel + 1];
        if (deflater == null) {
            boolean nowrap = true;
            deflater = new Deflater(compressionLevel, nowrap);
            this.deflaters[compressionLevel + 1] = deflater;
        }
        deflater.reset();
        return deflater;
    }
}
