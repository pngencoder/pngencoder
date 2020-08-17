package com.pngencoder;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PngEncoderBufferedImageTypeTest {
    @Test
    public void valueOfBufferedImage() {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
        PngEncoderBufferedImageType actual = PngEncoderBufferedImageType.valueOf(bufferedImage);
        PngEncoderBufferedImageType expected = PngEncoderBufferedImageType.TYPE_INT_ARGB_PRE;

        assertThat(actual, is(expected));
    }

    @Test
    public void valueOfTypeCustom() {
        PngEncoderBufferedImageType actual = PngEncoderBufferedImageType.valueOf(BufferedImage.TYPE_CUSTOM);
        PngEncoderBufferedImageType expected = PngEncoderBufferedImageType.TYPE_CUSTOM;

        assertThat(actual, is(expected));
    }

    @Test
    public void valueOfTypeIntArgb() {
        PngEncoderBufferedImageType actual = PngEncoderBufferedImageType.valueOf(BufferedImage.TYPE_INT_ARGB);
        PngEncoderBufferedImageType expected = PngEncoderBufferedImageType.TYPE_INT_ARGB;

        assertThat(actual, is(expected));
    }

    @Test
    public void valueOfAllInOrdinalLoop() {
        for (PngEncoderBufferedImageType expected : PngEncoderBufferedImageType.values()) {
            PngEncoderBufferedImageType actual = PngEncoderBufferedImageType.valueOf(expected.ordinal());
            assertThat(actual, is(expected));
        }
    }

    @Test
    public void containsAllBufferedImageTypes1() {
        new BufferedImage(1, 1, PngEncoderBufferedImageType.values().length - 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void containsAllBufferedImageTypes2() {
        new BufferedImage(1, 1, PngEncoderBufferedImageType.values().length);
    }

    @Test
    public void ToStringCombinesNameAndOrdinal() {
        String actual = PngEncoderBufferedImageType.TYPE_INT_ARGB.toString();
        String expected = "TYPE_INT_ARGB#2";

        assertThat(actual, is(expected));
    }
}
