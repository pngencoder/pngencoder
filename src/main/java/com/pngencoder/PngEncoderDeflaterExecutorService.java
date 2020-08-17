package com.pngencoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class PngEncoderDeflaterExecutorService {
    private static class Holder {
        private static final ExecutorService INSTANCE = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                PngEncoderDeflaterExecutorServiceThreadFactory.getInstance());
    }
    static ExecutorService getInstance() {
        return Holder.INSTANCE;
    }

    private PngEncoderDeflaterExecutorService() {
    }
}
