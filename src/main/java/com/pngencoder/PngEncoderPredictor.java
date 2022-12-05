package com.pngencoder;

import com.pngencoder.PngEncoderScanlineUtil.AbstractPNGLineConsumer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class PngEncoderPredictor {
    private PngEncoderPredictor() {
    }

    static void encodeImageMultiThreaded(BufferedImage image, PngEncoderScanlineUtil.EncodingMetaInfo metaInfo, OutputStream out) throws IOException {

        int height = image.getHeight();
        int heightPerSlice = Math.max(10, PngEncoderDeflaterOutputStream.SEGMENT_MAX_LENGTH_ORIGINAL_MIN / metaInfo.rowByteSize) + 1;

        /*
         * Encode the image in slices, so that we can stream some image rows into the CPU cache, and then
         * get them distributed to the ZIP threads without thrashing the cache to much.
         *
         * I.e. we stream heightPerSlices rows into the CPU cache and predictor encode it. Then we give pass the whole buffer
         * to the compressing output stream, which will spread it across CPU. That way the CPU data prefetch works well.
         *
         * If we directly write row by row into the compressing output stream we are going to trash the CPU cache too much.
         * Everytime the compression starts the data of the following rows - which are prefetched by the CPU - are
         * thrown out of the cache. Row by row was slower in benchmarks because of that.
         */
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream(heightPerSlice * metaInfo.rowByteSize);
        for (int y = 0; y < height; y += heightPerSlice) {
            int heightToProcess = Math.min(heightPerSlice, height - y);
            new PngEncoderPredictor().encodeImage(image, y, heightToProcess, metaInfo, outBytes);
            outBytes.writeTo(out);
            outBytes.reset();
        }
    }

    static void encodeImageSingleThreaded(BufferedImage image, PngEncoderScanlineUtil.EncodingMetaInfo metaInfo, OutputStream outputStream) throws IOException {
        new PngEncoderPredictor().encodeImage(image, 0, image.getHeight(), metaInfo, outputStream);
    }

    private byte[] dataRawRowSub;
    private byte[] dataRawRowUp;
    private byte[] dataRawRowAverage;
    private byte[] dataRawRowPaeth;

    private void encodeImage(BufferedImage image, int yStart, int height, PngEncoderScanlineUtil.EncodingMetaInfo metaInfo, OutputStream outputStream) throws IOException {
        dataRawRowSub = new byte[metaInfo.rowByteSize];
        dataRawRowUp = new byte[metaInfo.rowByteSize];
        dataRawRowAverage = new byte[metaInfo.rowByteSize];
        dataRawRowPaeth = new byte[metaInfo.rowByteSize];

        dataRawRowSub[0] = 1;
        dataRawRowUp[0] = 2;
        dataRawRowAverage[0] = 3;
        dataRawRowPaeth[0] = 4;

        boolean redoFirstRow = yStart > 0;
        PngEncoderScanlineUtil.stream(image, redoFirstRow ? (yStart - 1) : yStart, height + (redoFirstRow ? 1 : 0), new AbstractPNGLineConsumer() {
            boolean skipFirstRow = redoFirstRow;

            @Override
            void consume(byte[] currRow, byte[] prevRow) throws IOException {
                if (skipFirstRow) {
                    skipFirstRow = false;
                    return;
                }

                int bpp = metaInfo.bytesPerPixel;
                @SuppressWarnings("UnnecessaryLocalVariable")
                byte[] dataRawRowNone = currRow;
                byte[] dataRawRowSub = PngEncoderPredictor.this.dataRawRowSub;
                byte[] dataRawRowUp = PngEncoderPredictor.this.dataRawRowUp;
                byte[] dataRawRowAverage = PngEncoderPredictor.this.dataRawRowAverage;
                byte[] dataRawRowPaeth = PngEncoderPredictor.this.dataRawRowPaeth;

                // c | b
                // -----
                // a | x
                //
                // x => current pixel
                int bLen = currRow.length;
                assert currRow.length == prevRow.length;
                assert currRow[0] == 0;
                assert prevRow[0] == 0;

                long estCompressSum = 0;        // Marker 0 for no predictor
                long estCompressSumSub = 1;     // Marker 1 for sub predictor
                long estCompressSumUp = 2;      // Marker 2 for up predictor
                long estCompressSumAvg = 3;     // Marker 3 for average predictor
                long estCompressSumPaeth = 4;   // Marker 4 for paeth predictor

                int a = 0;
                int c = 0;
                for (int i = 1; i < bLen; i++) {
                    int x = currRow[i] & 0xFF;
                    int b = prevRow[i] & 0xFF;
                    if (i > bpp) {
                        int prevPixelByte = i - bpp;
                        a = currRow[prevPixelByte] & 0xFF;
                        c = prevRow[prevPixelByte] & 0xFF;
                    }

                    /*
                     * PNG Filters, see https://www.w3.org/TR/PNG-Filters.html
                     */
                    byte bSub = (byte) (x - a);
                    byte bUp = (byte) (x - b);
                    byte bAverage = (byte) (x - ((b + a) / 2));
                    byte bPaeth;
                    {
                        int p = a + b - c;
                        int pa = Math.abs(p - a);
                        int pb = Math.abs(p - b);
                        int pc = Math.abs(p - c);
                        final int pr;
                        if (pa <= pb && pa <= pc) {
                            pr = a;
                        } else if (pb <= pc) {
                            pr = b;
                        } else {
                            pr = c;
                        }

                        int r = x - pr;
                        bPaeth = (byte) r;
                    }

                    dataRawRowSub[i] = bSub;
                    dataRawRowUp[i] = bUp;
                    dataRawRowAverage[i] = bAverage;
                    dataRawRowPaeth[i] = bPaeth;

                    estCompressSum += Math.abs(x);
                    estCompressSumSub += Math.abs(bSub);
                    estCompressSumUp += Math.abs(bUp);
                    estCompressSumAvg += Math.abs(bAverage);
                    estCompressSumPaeth += Math.abs(bPaeth);
                }

                /*
                 * Choose which row to write
                 * https://www.w3.org/TR/PNG-Encoders.html#E.Filter-selection
                 */
                byte[] rowToWrite = dataRawRowNone;
                if (estCompressSum > estCompressSumSub) {
                    rowToWrite = dataRawRowSub;
                    estCompressSum = estCompressSumSub;
                }
                if (estCompressSum > estCompressSumUp) {
                    rowToWrite = dataRawRowUp;
                    estCompressSum = estCompressSumUp;
                }
                if (estCompressSum > estCompressSumAvg) {
                    rowToWrite = dataRawRowAverage;
                    estCompressSum = estCompressSumAvg;
                }
                if (estCompressSum > estCompressSumPaeth) {
                    rowToWrite = dataRawRowPaeth;
                }

                outputStream.write(rowToWrite);
            }
        });
    }
}
