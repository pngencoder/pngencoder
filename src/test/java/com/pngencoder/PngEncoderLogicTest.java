package com.pngencoder;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PngEncoderLogicTest {
    public static final String VALID_CHUNK_TYPE = "IDAT";

    @Test
    public void testAsChunkTypeNull() {
        assertThrows(NullPointerException.class, () -> PngEncoderLogic.asChunk(null, new byte[1]));
    }

    @Test
    public void testAsChunkTypeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> PngEncoderLogic.asChunk("chunk types must be four characters long", new byte[1]));
    }

    @Test
    public void testAsChunkNullBytes() {
        assertThrows(NullPointerException.class, () -> PngEncoderLogic.asChunk(VALID_CHUNK_TYPE, null));
    }

    @Test
    public void testAsChunkAdds12Bytes() {
        byte[] data = {5};
        byte[] chunk = PngEncoderLogic.asChunk(VALID_CHUNK_TYPE, data);

        int expected = 13;
        int actual = chunk.length;
        assertThat(actual, is(expected));
    }

    @Test
    public void testAsChunkFirstByteIsSizeOfData() {
        byte[] data = {5, 7};
        byte[] chunk = PngEncoderLogic.asChunk(VALID_CHUNK_TYPE, data);

        byte[] expected = intToBytes(data.length);
        assertThat(chunk[0], is(expected[0]));
        assertThat(chunk[1], is(expected[1]));
        assertThat(chunk[2], is(expected[2]));
        assertThat(chunk[3], is(expected[3]));
    }

    @Test
    public void testAsChunkCrcIsCalculatedFromTypeAndData() {
        byte[] data = {5, 7};
        byte[] chunk = PngEncoderLogic.asChunk(VALID_CHUNK_TYPE, data);

        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.mark();
        byteBuffer.put(VALID_CHUNK_TYPE.getBytes());
        byteBuffer.put(data);
        byteBuffer.reset();
        int expectedCrc = PngEncoderLogic.getCrc32(byteBuffer);

        byte[] expected = intToBytes(expectedCrc);
        assertThat(chunk[10], is(expected[0]));
        assertThat(chunk[11], is(expected[1]));
        assertThat(chunk[12], is(expected[2]));
        assertThat(chunk[13], is(expected[3]));
    }

    @Test
    public void testCrcWithPartialByteBuffer() {
        byte[] b = {5};

        ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        byteBuffer.put("garbage".getBytes());

        byteBuffer.mark();
        ByteBuffer slice = byteBuffer.slice().asReadOnlyBuffer();
        byteBuffer.put(b);
        slice.limit(b.length);
        byteBuffer.put("more garbage".getBytes());

        int actual = PngEncoderLogic.getCrc32(slice);
        int expected = getSimpleCrc(b);
        assertThat(actual, is(expected));
    }

    private static int getSimpleCrc(byte[] b) {
        CRC32 crc32 = new CRC32();
        crc32.update(b);
        return (int) crc32.getValue();
    }

    private static byte[] intToBytes(int val) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(val);
        return byteBuffer.array();
    }
}
