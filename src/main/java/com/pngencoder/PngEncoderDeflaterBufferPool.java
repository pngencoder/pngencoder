package com.pngencoder;

import java.util.LinkedList;
import java.util.Queue;

class PngEncoderDeflaterBufferPool {
    protected final Queue<PngEncoderDeflaterBuffer> buffers;

    PngEncoderDeflaterBufferPool() {
        this.buffers = new LinkedList<>();
    }

    PngEncoderDeflaterBuffer borrow() {
        PngEncoderDeflaterBuffer buffer = buffers.poll();
        if (buffer == null) {
            buffer = new PngEncoderDeflaterBuffer(this);
        }
        return buffer;
    }

    void giveBack(PngEncoderDeflaterBuffer buffer) {
        buffer.length = 0;
        buffers.offer(buffer);
    }

    int size() {
        return buffers.size();
    }
}
