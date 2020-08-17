package com.pngencoder;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

// https://tools.ietf.org/html/rfc1950
// https://stackoverflow.com/questions/9050260/what-does-a-zlib-header-look-like
// https://www.euccas.me/zlib/
// https://stackoverflow.com/questions/13132136/java-multithreaded-compression-with-deflater
class PngEncoderDeflaterOutputStream extends FilterOutputStream {
    private final PngEncoderDeflaterBufferPool pool;
    private final byte[] singleByte;
    private final int compressionLevel;
    private final ConcurrentLinkedQueue<CompletableFuture<PngEncoderDeflaterSegmentResult>> resultQueue;
    private final int maximumResultQueueSize;
    private PngEncoderDeflaterBuffer previousOriginalSegment;
    private PngEncoderDeflaterBuffer originalSegment;
    private long adler32;
    private boolean finished;
    private boolean closed;

    PngEncoderDeflaterOutputStream(OutputStream out, int compressionLevel, PngEncoderDeflaterBufferPool pool) throws IOException {
        super(Objects.requireNonNull(out, "out"));
        this.pool = Objects.requireNonNull(pool, "pool");
        this.singleByte = new byte[1];
        this.compressionLevel = compressionLevel;
        this.resultQueue = new ConcurrentLinkedQueue<>();
        this.maximumResultQueueSize = Runtime.getRuntime().availableProcessors() * 3;
        this.previousOriginalSegment = pool.borrow();
        this.originalSegment = pool.borrow();
        this.adler32 = 1;
        this.finished = false;
        this.closed = false;
        writeDeflateHeader(out, compressionLevel);
    }

    PngEncoderDeflaterOutputStream(OutputStream out, int compressionLevel) throws IOException {
        this(out, compressionLevel, new PngEncoderDeflaterBufferPool());
    }

    @Override
    public void write(int b) throws IOException {
        singleByte[0] = (byte)(b & 0xff);
        write(singleByte, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (finished) {
            throw new IOException("write beyond end of stream");
        }
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        while (len > 0) {
            int freeBufCount = PngEncoderLogic.SEGMENT_MAX_LENGTH_ORIGINAL - originalSegment.length;
            if (freeBufCount == 0) {
                // Submit task if the buffer is full and there still is more to write.
                joinUntilMaximumQueueSize(maximumResultQueueSize - 1);
                submitTask(false);
            } else {
                int toCopyCount = Math.min(len, freeBufCount);
                System.arraycopy(b, off, originalSegment.bytes, originalSegment.length, toCopyCount);
                originalSegment.length += toCopyCount;
                off += toCopyCount;
                len -= toCopyCount;
            }
        }
    }

    public void finish() throws IOException {
        if (this.finished) {
            return;
        }
        this.finished = true;
        try {
            submitTask(true);
            joinUntilMaximumQueueSize(0);
            out.write(ByteBuffer.allocate(4).putInt((int) adler32).array());
            out.flush();
        } finally {
            previousOriginalSegment.giveBack();
            originalSegment.giveBack();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        finish();
        super.close();
    }

    void submitTask(boolean lastSegment) {
        final PngEncoderDeflaterBuffer deflatedSegment = pool.borrow();
        final PngEncoderDeflaterSegmentTask task = new PngEncoderDeflaterSegmentTask(previousOriginalSegment, originalSegment, deflatedSegment, compressionLevel, lastSegment);
        submitTask(task);
        previousOriginalSegment = originalSegment;
        originalSegment = pool.borrow();
    }

    void submitTask(PngEncoderDeflaterSegmentTask task) {
        CompletableFuture<PngEncoderDeflaterSegmentResult> future = CompletableFuture.supplyAsync(task, PngEncoderDeflaterExecutorService.getInstance());
        resultQueue.offer(future);
    }

    void joinOne() throws IOException {
        CompletableFuture<PngEncoderDeflaterSegmentResult> resultFuture = resultQueue.poll();
        if (resultFuture != null) {
            final PngEncoderDeflaterSegmentResult result;
            try {
                result = resultFuture.join();
            } catch (RuntimeException e) {
                throw new IOException("An async segment task failed.", e);
            }
            try {
                adler32 = result.getUpdatedAdler32(adler32);
                result.getDeflatedSegment().write(out);
            } finally {
                result.getPreviousOriginalSegment().giveBack();
                result.getDeflatedSegment().giveBack();
            }
        }
    }

    void joinUntilMaximumQueueSize(int maximumResultQueueSize) throws IOException {
        while (resultQueue.size() > maximumResultQueueSize) {
            joinOne();
        }
    }

    static void writeDeflateHeader(OutputStream outputStream, int compressionLevel) throws IOException {
        // Write "CMF"
        // " ... In practice, this means the first byte is almost always 78 (hex) ..."
        outputStream.write(0x78);

        // Write "FLG"
        byte flg = getFlg(compressionLevel);
        outputStream.write(flg);
    }

    static byte getFlg(int compressionLevel) {
        if (compressionLevel == -1 || compressionLevel == 6) {
            return (byte) 0x9C;
        }

        // TODO: Is this really right?
        if (compressionLevel >= 0 && compressionLevel <= 1) {
            return (byte) 0x01;
        }

        if (compressionLevel >= 2 && compressionLevel <= 5) {
            return (byte) 0x5E;
        }

        if (compressionLevel >= 7 && compressionLevel <= 9) {
            return (byte) 0xDA;
        }

        throw new IllegalArgumentException("Invalid compressionLevel: " + compressionLevel);
    }
}
