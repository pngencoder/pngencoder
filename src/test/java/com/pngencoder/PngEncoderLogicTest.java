package com.pngencoder;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PngEncoderLogicTest {
    @Test
    public void segmentMaxLengthDictionaryIsExactly32k() {
        assertThat(PngEncoderLogic.SEGMENT_MAX_LENGTH_DICTIONARY, is(32 * 1024));
    }

    @Test
    public void segmentMaxLengthOriginalGreaterThanSegmentMaxLengthDictionary() {
        assertThat(PngEncoderLogic.SEGMENT_MAX_LENGTH_ORIGINAL, is(greaterThan(PngEncoderLogic.SEGMENT_MAX_LENGTH_DICTIONARY)));
    }

    @Test
    public void segmentMaxLengthDeflatedGreaterThanSegmentMaxLengthOriginal() {
        assertThat(PngEncoderLogic.SEGMENT_MAX_LENGTH_DEFLATED, is(greaterThan(PngEncoderLogic.SEGMENT_MAX_LENGTH_ORIGINAL)));
    }
}
