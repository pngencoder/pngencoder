package com.pngencoder;

import com.pngencoder.PngEncoderScanlineUtil.AbstractPNGLineConsumer;
import com.pngencoder.PngEncoderScanlineUtil.EncodingMetaInfo;
import com.pngencoder.PngEncoderScanlineUtil.EncodingMetaInfo.ColorSpaceType;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PngEncoderIndexed {
    static class IndexedEncoderResult {
        byte[] colorTable;
        byte[] transparencyTable;
        byte[] rawIDAT;
    }

    /**
     * Encode the image as indexed image. This can fail if the image has more than 256 colors. But we only know this if
     * we try to encode it. In case this image has more than 256 colors null is returned and everything written
     * into out has to disposed.
     *
     * @param image    the Image to encode
     * @param metaInfo the metaInfos of the image
     * @return null if this image can not be encoded as indexed image or the additional chunk data needed for the indexed image.
     * @throws IOException propagated IO Exception. Should not occur.
     */
    static IndexedEncoderResult encodeImage(BufferedImage image, EncodingMetaInfo metaInfo) throws IOException {
        /*
         * We only can encode 8 bit rgb image data here.
         */
		if (metaInfo.bitsPerChannel != 8 || metaInfo.channels == 1) {
			return null;
		}

        ByteArrayOutputStream out = new ByteArrayOutputStream(image.getHeight() * image.getWidth());

        IndexedEncoderResult result = new IndexedEncoderResult();

        // The first byte is 0; We don't try predictor encoding here. It's not worth the effort.
        byte[] indexedRow = new byte[image.getWidth() + 1];

        ColorTable table = new ColorTable();
        try {
            if (metaInfo.hasAlpha) {
                PngEncoderScanlineUtil.stream(image, 0, image.getHeight(), new AbstractPNGLineConsumer() {
                    boolean firstRow = true;

                    @Override
                    void consume(byte[] currRow, byte[] prevRow) throws IOException {
                        if (firstRow) {
                            int r = currRow[1] & 0xFF;
                            int g = currRow[2] & 0xFF;
                            int b = currRow[3] & 0xFF;
                            int a = currRow[4] & 0xFF;
                            int v = (a << 24) | (r << 16) | (g << 8) | b;
							if (a == 0) {
								v = 0;
							}
                            // initialise the first color slot
                            table.findColorLookup(v);
                            firstRow = false;
                        }

                        int readPtr = 1; // Skip predictor setting byte
                        for (int x = 1; x < indexedRow.length; x++) {
                            int r = currRow[readPtr++] & 0xFF;
                            int g = currRow[readPtr++] & 0xFF;
                            int b = currRow[readPtr++] & 0xFF;
                            int a = currRow[readPtr++] & 0xFF;
                            int v;
							if (a == 0) {
								v = 0;
							} else {
								v = (a << 24) | (r << 16) | (g << 8) | b;
							}
                            indexedRow[x] = table.findColor(v);
                        }

                        out.write(indexedRow);
                    }
                });
            } else {
                PngEncoderScanlineUtil.stream(image, 0, image.getHeight(), new AbstractPNGLineConsumer() {
                    boolean firstRow = true;

                    @Override
                    void consume(byte[] currRow, byte[] prevRow) throws IOException {
                        if (firstRow) {
                            int r = currRow[1] & 0xFF;
                            int g = currRow[2] & 0xFF;
                            int b = currRow[3] & 0xFF;
                            int v = 0xFF000000 | (r << 16) | (g << 8) | b;
                            // initialise the first color slot
                            table.findColorLookup(v);
                            firstRow = false;
                        }

                        int readPtr = 1; // Skip predictor setting byte
                        for (int x = 1; x < indexedRow.length; x++) {
                            int r = currRow[readPtr++] & 0xFF;
                            int g = currRow[readPtr++] & 0xFF;
                            int b = currRow[readPtr++] & 0xFF;
                            int v = 0xFF000000 | (r << 16) | (g << 8) | b;
                            indexedRow[x] = table.findColor(v);
                        }

                        out.write(indexedRow);
                    }
                });
            }
        } catch (IndexOutOfBoundsException ignored) {
            // We have more than 256 colors ...
            return null;
        }

        result.rawIDAT = out.toByteArray();
        result.colorTable = table.makeColorTable();
		if (metaInfo.hasAlpha) {
			result.transparencyTable = table.makeTransparencyTable();
		}
        metaInfo.colorSpaceType = ColorSpaceType.Indexed;
        metaInfo.rowByteSize = indexedRow.length;
        return result;
    }

    private static class ColorTable {
        int[] colorTable = new int[256];
        int usedColors = 0;
        int lastColor;
        int lastColorIndex = 0;

        byte findColor(int color) {
			if (lastColor == color) {
				return (byte) lastColorIndex;
			}
            return (byte) findColorLookup(color);
        }

        private int findColorLookup(int color) {
            for (int i = 0; i < usedColors; i++) {
                if (colorTable[i] == color) {
                    lastColor = color;
                    lastColorIndex = i;
                    return i;
                }
            }

            int colorIndex = usedColors++;
            colorTable[colorIndex] = color;
            lastColor = color;
            lastColorIndex = colorIndex;
            return colorIndex;
        }

        public byte[] makeColorTable() {
            byte[] res = new byte[3 * usedColors];
            int dest = 0;
            for (int i = 0; i < usedColors; i++) {
                int color = colorTable[i];
                res[dest++] = (byte) ((color & 0x00FF0000) >> 16);
                res[dest++] = (byte) ((color & 0x0000FF00) >> 8);
                res[dest++] = (byte) ((color & 0x000000FF));
            }
            return res;
        }

        public byte[] makeTransparencyTable() {
            byte[] res = new byte[usedColors];
            int dest = 0;
            for (int i = 0; i < usedColors; i++) {
                int color = colorTable[i];
                res[dest++] = (byte) ((color & 0xFF000000) >> 24);
            }
            return res;
        }
    }
}
