package com.pngencoder;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PngEncoderDeflaterOutputStreamTest {
    private static final int SEGMENT_MAX_LENGTH_ORIGINAL = 64 * 1024;

    private static final BiConsumer<byte[], OutputStream> SINGLE_THREADED_DEFLATER = (bytes, outputStream) -> {
        try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream)) {
            deflaterOutputStream.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };

    private static final BiConsumer<byte[], OutputStream> MULTI_THREADED_DEFLATER = (bytes, outputStream) -> {
        try (PngEncoderDeflaterOutputStream deflaterOutputStream = new PngEncoderDeflaterOutputStream(outputStream, PngEncoder.DEFAULT_COMPRESSION_LEVEL, SEGMENT_MAX_LENGTH_ORIGINAL)) {
            deflaterOutputStream.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    };

    @Test
    public void deflateSingleThreadedJustFiveBytes() throws Exception {
        byte[] expected = { 1, 2, 3, 4, 5 };
        assertThatBytesIsSameAfterDeflateAndInflate(expected, SINGLE_THREADED_DEFLATER);
    }

    @Test
    public void deflateMultiThreadedJustFiveBytes() throws Exception {
        byte[] expected = { 1, 2, 3, 4, 5 };
        assertThatBytesIsSameAfterDeflateAndInflate(expected, MULTI_THREADED_DEFLATER);
    }

    @Test
    public void deflateSingleThreadedOneSegment() throws Exception {
        byte[] expected = createRandomBytes(SEGMENT_MAX_LENGTH_ORIGINAL / 2);
        assertThatBytesIsSameAfterDeflateAndInflate(expected, SINGLE_THREADED_DEFLATER);
    }

    @Test
    public void deflateMultiThreadedOneSegment() throws Exception {
        byte[] expected = createRandomBytes(SEGMENT_MAX_LENGTH_ORIGINAL / 2);
        assertThatBytesIsSameAfterDeflateAndInflate(expected, MULTI_THREADED_DEFLATER);
    }

    @Test
    public void deflateSingleThreadedTwoSegmentsToTestSegmentBoundary() throws Exception {
        byte[] expected = createRandomBytes(SEGMENT_MAX_LENGTH_ORIGINAL * 2);
        assertThatBytesIsSameAfterDeflateAndInflate(expected, SINGLE_THREADED_DEFLATER);
    }

    @Test
    public void deflateMultiThreadedTwoSegmentsToTestSegmentBoundary() throws Exception {
        byte[] expected = createRandomBytes(SEGMENT_MAX_LENGTH_ORIGINAL * 2);
        assertThatBytesIsSameAfterDeflateAndInflate(expected, MULTI_THREADED_DEFLATER);
    }

    @Test
    public void deflateMultiThreaded300SegmentsToTestThreadSafety() throws Exception {
        byte[] expected = createRandomBytes(SEGMENT_MAX_LENGTH_ORIGINAL * 300);
        assertThatBytesIsSameAfterDeflateAndInflateFast(expected, MULTI_THREADED_DEFLATER);
    }

    @Test(expected = IOException.class)
    public void constructorThrowsIOExceptionOnWritingDeflateHeaderWithRiggedOutputStream() throws IOException {
        RiggedOutputStream riggedOutputStream = new RiggedOutputStream(1);
        new PngEncoderDeflaterOutputStream(riggedOutputStream, PngEncoder.DEFAULT_COMPRESSION_LEVEL, SEGMENT_MAX_LENGTH_ORIGINAL);
    }

    @Test(expected = IOException.class)
    public void finishThrowsIOExceptionOnJoiningWithRiggedOutputStream() throws IOException {
        RiggedOutputStream riggedOutputStream = new RiggedOutputStream(3);
        PngEncoderDeflaterOutputStream deflaterOutputStream = new PngEncoderDeflaterOutputStream(riggedOutputStream, PngEncoder.DEFAULT_COMPRESSION_LEVEL, SEGMENT_MAX_LENGTH_ORIGINAL);
        byte[] bytesToWrite = createRandomBytes(10);
        deflaterOutputStream.write(bytesToWrite);
        deflaterOutputStream.finish();
    }

    @Test(expected = IOException.class)
    public void finishThrowsIOExceptionOnJoiningWithRiggedPngEncoderDeflaterSegmentTask() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PngEncoderDeflaterOutputStream deflaterOutputStream = new PngEncoderDeflaterOutputStream(byteArrayOutputStream, PngEncoder.DEFAULT_COMPRESSION_LEVEL, SEGMENT_MAX_LENGTH_ORIGINAL);
        deflaterOutputStream.submitTask(new RiggedPngEncoderDeflaterSegmentTask());
        deflaterOutputStream.finish();
    }

    @Test
    public void assertiveBufferPool10Bytes() throws IOException {
        PngEncoderDeflaterBufferPoolAssertive pool = new PngEncoderDeflaterBufferPoolAssertive(PngEncoderDeflaterOutputStream.getSegmentMaxLengthDeflated(SEGMENT_MAX_LENGTH_ORIGINAL));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PngEncoderDeflaterOutputStream deflaterOutputStream = new PngEncoderDeflaterOutputStream(outputStream, PngEncoder.DEFAULT_COMPRESSION_LEVEL, SEGMENT_MAX_LENGTH_ORIGINAL, pool);
        byte[] bytesToWrite = createRandomBytes(10);
        deflaterOutputStream.write(bytesToWrite);
        deflaterOutputStream.finish();
        pool.assertThatGivenIsBorrowed();
    }

    @Test
    public void assertiveBufferPoolManyBytes() throws IOException {
        PngEncoderDeflaterBufferPoolAssertive pool = new PngEncoderDeflaterBufferPoolAssertive(PngEncoderDeflaterOutputStream.getSegmentMaxLengthDeflated(SEGMENT_MAX_LENGTH_ORIGINAL));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PngEncoderDeflaterOutputStream deflaterOutputStream = new PngEncoderDeflaterOutputStream(outputStream, PngEncoder.DEFAULT_COMPRESSION_LEVEL, SEGMENT_MAX_LENGTH_ORIGINAL, pool);
        byte[] bytesToWrite = createRandomBytes(SEGMENT_MAX_LENGTH_ORIGINAL * 2);
        deflaterOutputStream.write(bytesToWrite);
        deflaterOutputStream.finish();
        pool.assertThatGivenIsBorrowed();
    }

    @Test
    public void segmentMaxLengthDictionaryIsExactly32k() {
        assertThat(PngEncoderDeflaterOutputStream.SEGMENT_MAX_LENGTH_DICTIONARY, is(32 * 1024));
    }

    @Test
    public void segmentMaxLengthOriginalMinGreaterThanSegmentMaxLengthDictionary() {
        assertThat(PngEncoderDeflaterOutputStream.SEGMENT_MAX_LENGTH_ORIGINAL_MIN, is(greaterThan(PngEncoderDeflaterOutputStream.SEGMENT_MAX_LENGTH_DICTIONARY)));
    }

    @Test
    public void segmentMaxLengthDeflatedGreaterThanSegmentMaxLengthOriginal() {
        final int segmentMaxLengthOriginal = 64 * 1024;
        final int segmentMaxLengthDeflated = PngEncoderDeflaterOutputStream.getSegmentMaxLengthDeflated(segmentMaxLengthOriginal);

        assertThat(segmentMaxLengthDeflated, is(greaterThan(segmentMaxLengthOriginal)));
    }

    @Test
    public void getSegmentMaxLengthOriginalRespectsMin() {
        final int actual = PngEncoderDeflaterOutputStream.getSegmentMaxLengthOriginal(1);
        final int expected = PngEncoderDeflaterOutputStream.SEGMENT_MAX_LENGTH_ORIGINAL_MIN;

        assertThat(actual, is(expected));
    }

    @Test
    public void getSegmentMaxLengthOriginalDoesNotIncreaseImmediatelyOverMin() {
        final int actual = PngEncoderDeflaterOutputStream.getSegmentMaxLengthOriginal(PngEncoderDeflaterOutputStream.SEGMENT_MAX_LENGTH_ORIGINAL_MIN + 1);
        final int expected = PngEncoderDeflaterOutputStream.SEGMENT_MAX_LENGTH_ORIGINAL_MIN;

        assertThat(actual, is(expected));
    }

    private static byte[] createRandomBytes(int length) {
        Random random = new Random(12345);
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);
        return randomBytes;
    }

    private static void assertThatBytesIsSameAfterDeflateAndInflate(byte[] expected, BiConsumer<byte[], OutputStream> deflater) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        deflater.accept(expected, outputStream);
        byte[] deflated = outputStream.toByteArray();
        byte[] actual = inflate(deflated);
        assertThat(actual.length, is(expected.length));
        assertThat(actual, is(expected));
    }

    private static void assertThatBytesIsSameAfterDeflateAndInflateFast(byte[] expected, BiConsumer<byte[], OutputStream> deflater) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        deflater.accept(expected, outputStream);
        byte[] deflated = outputStream.toByteArray();
        byte[] actual = inflate(deflated);
        assertThat(actual.length, is(expected.length));
        for (int i = 0; i < actual.length; i+=11) {
            assertThat(actual[i], is(expected[i]));
        }
    }

    private static byte[] inflate(byte[] deflated) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(byteArrayOutputStream)) {
            inflaterOutputStream.write(deflated);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static class PngEncoderDeflaterBufferPoolAssertive extends PngEncoderDeflaterBufferPool {
        private final Set<PngEncoderDeflaterBuffer> borrowed = Collections.newSetFromMap(new ConcurrentHashMap<>());
        private final Set<PngEncoderDeflaterBuffer> given = Collections.newSetFromMap(new ConcurrentHashMap<>());

        public PngEncoderDeflaterBufferPoolAssertive(int bufferMaxLength) {
            super(bufferMaxLength);
        }

        @Override
        PngEncoderDeflaterBuffer borrow() {
            PngEncoderDeflaterBuffer buffer = super.borrow();
            borrowed.add(buffer);
            return buffer;
        }

        @Override
        void giveBack(PngEncoderDeflaterBuffer buffer) {
            if (buffers.contains(buffer)) {
                throw new IllegalArgumentException("Adding an already present buffer to pool is not allowed.");
            }
            given.add(buffer);
            super.giveBack(buffer);
        }

        void assertThatGivenIsBorrowed() {
            assertThat(given, is(borrowed));
        }
    }

    private static class RiggedOutputStream extends OutputStream {
        private final int countBytesToThrowException;
        private int count;

        public RiggedOutputStream(int countBytesToThrowException) {
            this.countBytesToThrowException = countBytesToThrowException;
            this.count = 0;
        }

        @Override
        public void write(int b) throws IOException {
            count++;
            if (count >= countBytesToThrowException) {
                throw new IOException("This exception was generated for the purpose of testing.");
            }
        }
    }

    private static class RiggedPngEncoderDeflaterSegmentTask extends PngEncoderDeflaterSegmentTask {
        private static final PngEncoderDeflaterBufferPool pool = new PngEncoderDeflaterBufferPool(1337);

        public RiggedPngEncoderDeflaterSegmentTask() {
            super(pool.borrow(), pool.borrow(), PngEncoder.DEFAULT_COMPRESSION_LEVEL, false);
        }

        @Override
        public PngEncoderDeflaterSegmentResult get() {
            throw new RuntimeException("This exception was generated for the purpose of testing.");
        }
    }
}
