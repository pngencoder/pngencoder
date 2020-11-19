package com.pngencoder;

/**
 * Represents PNG physical piel dimensions
 *
 * Use on of the static methods to create physical pixel dimensions based
 * on pixels per meter, dots per inch or a unit-less aspect ratio.
 *
 * @see <a href="https://www.w3.org/TR/PNG/#11pHYs">https://www.w3.org/TR/PNG/#11pHYs</a>
 */
public class PngEncoderPhysicalPixelDimensions {

    public enum Unit {
        UKNOWN((byte) 0),
        METER((byte) 1);

        private final byte value;

        Unit(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }

    private static final float INCHES_PER_METER = 100 / 2.54f;

    private final int pixelsPerUnitX;
    private final int pixelsPerUnitY;
    private final Unit unit;

    private PngEncoderPhysicalPixelDimensions(int pixelsPerUnitX, int pixelsPerUnitY, Unit unit) {
        this.pixelsPerUnitX = pixelsPerUnitX;
        this.pixelsPerUnitY = pixelsPerUnitY;
        this.unit = unit;
    }

    public static PngEncoderPhysicalPixelDimensions pixelsPerMeter(int pixelsPerMeterX, int pixelsPerMeterY) {
        return new PngEncoderPhysicalPixelDimensions(pixelsPerMeterX, pixelsPerMeterY, Unit.METER);
    }

    public static PngEncoderPhysicalPixelDimensions pixelsPerMeter(int pixelsPerMeter) {
        return pixelsPerMeter(pixelsPerMeter, pixelsPerMeter);
    }

    public static PngEncoderPhysicalPixelDimensions dotsPerInch(int dotsPerInchX, int dotsPerInchY) {
        int pixelsPerMeterX = Math.round(dotsPerInchX * INCHES_PER_METER);
        int pixelsPerMeterY = Math.round(dotsPerInchY * INCHES_PER_METER);

        return new PngEncoderPhysicalPixelDimensions(pixelsPerMeterX, pixelsPerMeterY, Unit.METER);
    }

    public static PngEncoderPhysicalPixelDimensions dotsPerInch(int dotsPerInch) {
        return dotsPerInch(dotsPerInch, dotsPerInch);
    }

    public static PngEncoderPhysicalPixelDimensions aspectRatio(int pixelsPerUnitX, int pixelsPerUnitY) {
        return new PngEncoderPhysicalPixelDimensions(pixelsPerUnitX, pixelsPerUnitY, Unit.UKNOWN);
    }

    public int getPixelsPerUnitX() {
        return pixelsPerUnitX;
    }

    public int getPixelsPerUnitY() {
        return pixelsPerUnitY;
    }

    public Unit getUnit() {
        return unit;
    }
}
