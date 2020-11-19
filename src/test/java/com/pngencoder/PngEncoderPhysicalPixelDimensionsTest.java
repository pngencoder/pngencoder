package com.pngencoder;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PngEncoderPhysicalPixelDimensionsTest {

    @Test
    public void pixelsPerMeter() {
        PngEncoderPhysicalPixelDimensions physicalPixelDimensions =
                PngEncoderPhysicalPixelDimensions.pixelsPerMeter(2835);

        assertThat(physicalPixelDimensions.getPixelsPerUnitX(), is(2835));
        assertThat(physicalPixelDimensions.getPixelsPerUnitY(), is(2835));
        assertThat(physicalPixelDimensions.getUnit(), is(PngEncoderPhysicalPixelDimensions.Unit.METER));
    }

    @Test
    public void aspectRatio() {
        PngEncoderPhysicalPixelDimensions physicalPixelDimensions =
                PngEncoderPhysicalPixelDimensions.aspectRatio(2, 3);

        assertThat(physicalPixelDimensions.getPixelsPerUnitX(), is(2));
        assertThat(physicalPixelDimensions.getPixelsPerUnitY(), is(3));
        assertThat(physicalPixelDimensions.getUnit(), is(PngEncoderPhysicalPixelDimensions.Unit.UKNOWN));
    }

    @Test
    public void dpi() {
        PngEncoderPhysicalPixelDimensions physicalPixelDimensions =
                PngEncoderPhysicalPixelDimensions.dotsPerInch(72);

        assertThat(physicalPixelDimensions.getPixelsPerUnitX(), is(2835));
        assertThat(physicalPixelDimensions.getPixelsPerUnitY(), is(2835));
        assertThat(physicalPixelDimensions.getUnit(), is(PngEncoderPhysicalPixelDimensions.Unit.METER));
    }
}
