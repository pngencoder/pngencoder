package com.pngencoder;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PngEncoderDeflaterBufferPoolTest {
    @Test
    public void initialSizeIsZero() {
        final PngEncoderDeflaterBufferPool bufferPool = new PngEncoderDeflaterBufferPool();
        final int actual = bufferPool.size();
        final int expected = 0;
        assertThat(actual, is(expected));
    }

    @Test
    public void sizeIsZeroAfterBorrowingTwiceAndNotGivingBack() {
        final PngEncoderDeflaterBufferPool bufferPool = new PngEncoderDeflaterBufferPool();
        bufferPool.borrow();
        bufferPool.borrow();
        final int actual = bufferPool.size();
        final int expected = 0;
        assertThat(actual, is(expected));
    }

    @Test
    public void sizeIs1AfterBorrowingAndGivingBackTwice() {
        final PngEncoderDeflaterBufferPool bufferPool = new PngEncoderDeflaterBufferPool();

        PngEncoderDeflaterBuffer borrowed1 = bufferPool.borrow();
        borrowed1.giveBack();

        PngEncoderDeflaterBuffer borrowed2 = bufferPool.borrow();
        borrowed2.giveBack();

        final int actual = bufferPool.size();
        final int expected = 1;
        assertThat(actual, is(expected));
    }

    @Test
    public void bufferBytesLengthIsSegmentMaxLengthDeflated() {
        final PngEncoderDeflaterBufferPool bufferPool = new PngEncoderDeflaterBufferPool();
        PngEncoderDeflaterBuffer borrowed = bufferPool.borrow();
        final int actual = borrowed.bytes.length;
        final int expected = PngEncoderLogic.SEGMENT_MAX_LENGTH_DEFLATED;
        assertThat(actual, is(expected));
    }
}
