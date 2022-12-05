package com.pngencoder;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

class PngEncoderScanlineUtil {
    private PngEncoderScanlineUtil() {
    }

    /**
     * Consumer for the image rows as bytes. Every row has the predictor marker as
     * first byte (with 0 for no predictor encoding), after that all image bytes
     * follow.
     * <p>
     * This is a class and not an interface for performance reasons. So that the JVM can
     * fall back to simple vtable call in the polymorphic call site. As you can read on
     * <a href="https://wiki.openjdk.java.net/display/HotSpot/InterfaceCalls">https://wiki.openjdk.java.net/display/HotSpot/InterfaceCalls</a>
     * interface calls are very expensive and not suitable for performance critical sites, which can
     * be polymorphic.
     */
    static abstract class AbstractPNGLineConsumer {
        /**
         * Consume and encode a row bytes consisting of image bytes.
         *
         * @param currRow the current row which should be encoded in the image stream
         * @param prevRow the previous row, which is required for predictor encoding.
         *                Is complete filled with 0 when encoding the first row.
         * @throws IOException if some IO error happens
         */
        abstract void consume(byte[] currRow, byte[] prevRow) throws IOException;
    }

    /**
     * Consumer getting everything as big byte array. Only used to implement get().
     * <p>
     * This thrashes the CPU cache, as with bigger images the whole image data will
     * not fit into the cache and has to be fetched again from main memory when future
     * processing the data.
     */
    static class ByteBufferPNGLineConsumer extends AbstractPNGLineConsumer {
        byte[] bytes;
        int currentOffset;

        ByteBufferPNGLineConsumer(int byteCount) {
            bytes = new byte[byteCount];
        }

        void consume(byte[] currRow, byte[] prevRow) {
            System.arraycopy(currRow, 0, bytes, currentOffset, currRow.length);
            currentOffset += currRow.length;
        }
    }

    /**
     * Metadata about how the image has to be encoded.
     */
    static class EncodingMetaInfo {
        /**
         * Count of color channels. Can either be 1 (for gray), 2 (gray with alpha), 3 (rgb), 4 (rgb with alpha)
         */
        int channels;
        /**
         * Of how many bytes does a pixel have? This is needed for the predictor.
         */
        int bytesPerPixel;
        /**
         * Bits per channel, can be 8 or 16
         */
        int bitsPerChannel = 8;
        /**
         * Size of a row consumed by AbstractPNGLineConsumer::consume() including a
         * 1 byte marker for the predictor.
         */
        int rowByteSize;
        /**
         * Do we have a alpha channel?
         */
        boolean hasAlpha;
        /**
         * If not null we must embed this color profile in the PNG file.
         * It can only be null for sRGB images.
         */
        ICC_Profile colorProfile;

        enum ColorSpaceType {
            Rgb,
            Gray,
            Indexed
        }

        /**
         * The kind of color space used in the image
         */
        ColorSpaceType colorSpaceType;
    }

    /*
     * Get the encoding metadata
     */
    static EncodingMetaInfo getEncodingMetaInfo(BufferedImage bufferedImage) {
        EncodingMetaInfo info = new EncodingMetaInfo();
        int width = bufferedImage.getWidth();
        final PngEncoderBufferedImageType type = PngEncoderBufferedImageType.valueOf(bufferedImage);
        ColorSpace colorSpace = bufferedImage.getColorModel().getColorSpace();

        if (!colorSpace.isCS_sRGB() && colorSpace instanceof ICC_ColorSpace) {
            info.colorProfile = ((ICC_ColorSpace) colorSpace).getProfile();
        }

        info.colorSpaceType = !colorSpace.isCS_sRGB() && colorSpace instanceof ICC_ColorSpace && colorSpace.getType() == ColorSpace.TYPE_GRAY ?
                EncodingMetaInfo.ColorSpaceType.Gray :
                EncodingMetaInfo.ColorSpaceType.Rgb;

        switch (type) {
            case TYPE_INT_ARGB:
            case TYPE_INT_ARGB_PRE:
            case TYPE_4BYTE_ABGR:
            case TYPE_4BYTE_ABGR_PRE:
                info.channels = 4;
                info.bytesPerPixel = 4;
                info.hasAlpha = true;
                break;
            case TYPE_INT_BGR:
            case TYPE_3BYTE_BGR:
            case TYPE_USHORT_565_RGB:
            case TYPE_USHORT_555_RGB:
            case TYPE_INT_RGB:
                info.channels = 3;
                info.bytesPerPixel = 3;
                break;
            case TYPE_BYTE_GRAY:
                info.channels = 1;
                info.bytesPerPixel = 1;
                break;
            case TYPE_USHORT_GRAY:
                info.channels = 1;
                info.bytesPerPixel = 2;
                info.bitsPerChannel = 16;
                break;
            default:
                info.hasAlpha = bufferedImage.getTransparency() != Transparency.OPAQUE; // TODO: This doesn't look right. What if the value is Transparency.BITMASK?

                /*
                 * Default sRGB byte encoding
                 */
                if (!info.hasAlpha) {
                    info.channels = 3;
                    info.bytesPerPixel = 3;
                } else {
                    info.channels = 4;
                    info.bytesPerPixel = 4;
                }

                /*
                 * We can only handle RGB and GRAY in png. CMYK etc. is not in the spec...
                 */
                final boolean needToFallBackTosRGB = !colorSpace.isCS_sRGB() && colorSpace instanceof ICC_ColorSpace && colorSpace.getType() != ColorSpace.TYPE_RGB && colorSpace.getType() != ColorSpace.TYPE_GRAY;
                /*
                 * When it is a UShort GRAY or RGB buffer we can write it as 16 bit image.
                 */
                boolean canICCBeHandled = false;
                if (!needToFallBackTosRGB && bufferedImage.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT) {
                    info.channels = bufferedImage.getRaster().getSampleModel().getNumBands();
                    info.bytesPerPixel = info.channels * 2;
                    info.bitsPerChannel = 16;
                    canICCBeHandled = true;
                }
                /*
                 * Custom Int Buffers storing 8 bit RGB
                 */
                if (!needToFallBackTosRGB && bufferedImage.getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_INT
                        && bufferedImage.getSampleModel().getSampleSize(0) == 8) {
                    canICCBeHandled = true;
                }

                /*
                 * When we can not handle the data buffer we have to fall back to sRGB. So we should not include a color profile
                 */
                if (!canICCBeHandled) {
                    info.colorProfile = null;
                }
                break;
        }

        info.rowByteSize = 1 + info.bytesPerPixel * width;
        return info;
    }

    static byte[] get(BufferedImage bufferedImage) throws IOException {
        final int height = bufferedImage.getHeight();
        EncodingMetaInfo encodingMetaInfo = getEncodingMetaInfo(bufferedImage);
        ByteBufferPNGLineConsumer consumer = new ByteBufferPNGLineConsumer(encodingMetaInfo.rowByteSize * height);
        stream(bufferedImage, 0, height, consumer);
        return consumer.bytes;
    }

    /**
     * Stream image rows to a consumer, row by row.
     */
    static void stream(BufferedImage bufferedImage, int yStart, int heightToStream, AbstractPNGLineConsumer consumer)
            throws IOException {
        final int width = bufferedImage.getWidth();
        final int imageHeight = bufferedImage.getHeight();
        assert (heightToStream <= imageHeight - yStart);

        final PngEncoderBufferedImageType type = PngEncoderBufferedImageType.valueOf(bufferedImage);

        WritableRaster raster = bufferedImage.getRaster();
        switch (type) {
            case TYPE_INT_RGB:
                getIntRgb(raster, yStart, width, heightToStream, consumer);
                break;
            case TYPE_INT_ARGB:
                getIntArgb(raster, yStart, width, heightToStream, false, consumer);
                break;
            case TYPE_INT_ARGB_PRE:
                getIntArgb(raster, yStart, width, heightToStream, true, consumer);
                break;
            case TYPE_INT_BGR:
                getIntBgr(raster, yStart, width, heightToStream, consumer);
                break;
            case TYPE_3BYTE_BGR:
                get3ByteBgr(raster, yStart, width, heightToStream, consumer);
                break;
            case TYPE_4BYTE_ABGR:
                get4ByteAbgr(raster, yStart, width, heightToStream, false, consumer);
                break;
            case TYPE_4BYTE_ABGR_PRE:
                get4ByteAbgr(raster, yStart, width, heightToStream, true, consumer);
                break;
            // TODO: TYPE_USHORT_565_RGB
            // TODO: TYPE_USHORT_555_RGB
            case TYPE_BYTE_GRAY:
                getByteGray(bufferedImage, yStart, width, heightToStream, consumer);
                break;
            case TYPE_USHORT_GRAY:
                getUshortGray(bufferedImage, yStart, width, heightToStream, consumer);
                break;
            case TYPE_BYTE_INDEXED:
                getFallback(bufferedImage, yStart, width, heightToStream, consumer);
                break;
            default:
                if (raster.getDataBuffer() instanceof DataBufferUShort) {
                    if (getUshortGenericDataBufferUShort(bufferedImage, yStart, width, heightToStream, consumer)) {
                        break;
                    }
                }
                // Generic DataBuffer variants.
                if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_USHORT) {
                    if (getUshortGeneric(bufferedImage, yStart, width, heightToStream, consumer)) {
                        break;
                    }
                }
                if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE) {
                    if (getByteGeneric(bufferedImage, yStart, width, heightToStream, consumer)) {
                        break;
                    }
                }
                if (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_INT) {
                    if (getIntGeneric(bufferedImage, yStart, width, heightToStream, consumer)) {
                        break;
                    }
                }

                getFallback(bufferedImage, yStart, width, heightToStream, consumer);
                break;
        }
    }

    /**
     * Fallback for unsupported types. We use getRGB, which will convert the image.
     */
    private static void getFallback(BufferedImage bufferedImage, int yStart, int width, int heightToStream, AbstractPNGLineConsumer consumer) throws IOException {
        final int[] elements = bufferedImage.getRGB(0, yStart, width, heightToStream, null, 0, width);
        if (bufferedImage.getTransparency() == Transparency.OPAQUE) {
            getIntRgb(elements, yStart, width, heightToStream, consumer);
        } else {
            getIntArgb(elements, yStart, width, heightToStream, consumer);
        }
    }

    static void getIntRgb(int[] elements, int yStart, int width, int height, AbstractPNGLineConsumer consumer)
            throws IOException {
        final int channels = 3;
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        for (int y = yStart; y < yStart + height; y++) {
            int yOffset = y * width;

            int rowByteOffset = 1;

            for (int x = 0; x < width; x++) {
                final int element = elements[yOffset + x];
                currLine[rowByteOffset++] = (byte) (element >> 16); // R
                currLine[rowByteOffset++] = (byte) (element >> 8); // G
                currLine[rowByteOffset++] = (byte) element; // B
            }
            consumer.consume(currLine, prevLine);
            {
                byte[] b = currLine;
                currLine = prevLine;
                prevLine = b;
            }
        }
    }

    static void getIntArgb(int[] elements, int yStart, int width, int height, AbstractPNGLineConsumer consumer)
            throws IOException {
        final int channels = 4;
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        for (int y = yStart; y < yStart + height; y++) {
            int yOffset = y * width;

            int rowByteOffset = 1;
            for (int x = 0; x < width; x++) {
                final int element = elements[yOffset + x];
                currLine[rowByteOffset++] = (byte) (element >> 16); // R
                currLine[rowByteOffset++] = (byte) (element >> 8); // G
                currLine[rowByteOffset++] = (byte) element; // B
                currLine[rowByteOffset++] = (byte) (element >> 24); // A
            }
            consumer.consume(currLine, prevLine);
            {
                byte[] b = currLine;
                currLine = prevLine;
                prevLine = b;
            }
        }
    }

    static void getIntRgb(WritableRaster imageRaster, int yStart, int width, int heightToStream,
            AbstractPNGLineConsumer consumer) throws IOException {
        final int channels = 3;
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        if (imageRaster.getSampleModel() instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sampleModel = (SinglePixelPackedSampleModel) imageRaster.getSampleModel();
            int scanlineStride = sampleModel.getScanlineStride();
            assert sampleModel.getNumBands() == 3;
            assert sampleModel.getBitOffsets()[0] == 16;
            assert sampleModel.getBitOffsets()[1] == 8;
            assert sampleModel.getBitOffsets()[2] == 0;
            int[] rawInts = ((DataBufferInt) imageRaster.getDataBuffer()).getData();

            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX();
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;
                int pixelEndPtr = linePtr + width;

                int rowByteOffset = 1;
                while (pixelPtr < pixelEndPtr) {
                    final int element = rawInts[pixelPtr++];
                    currLine[rowByteOffset++] = (byte) (element >> 16); // R
                    currLine[rowByteOffset++] = (byte) (element >> 8); // G
                    currLine[rowByteOffset++] = (byte) element; // B
                }

                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);

                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
        } else {
            throw new IllegalStateException("TYPE_INT_RGB must have a SinglePixelPackedSampleModel");
        }
    }

    static void getIntArgb(WritableRaster imageRaster, int yStart, int width, int heightToStream,
            boolean preMultipliedAlpha, AbstractPNGLineConsumer consumer) throws IOException {
        final int channels = 4;
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        if (imageRaster.getSampleModel() instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sampleModel = (SinglePixelPackedSampleModel) imageRaster.getSampleModel();
            int scanlineStride = sampleModel.getScanlineStride();
            assert sampleModel.getNumBands() == 4;
            assert sampleModel.getBitOffsets()[0] == 16;
            assert sampleModel.getBitOffsets()[1] == 8;
            assert sampleModel.getBitOffsets()[2] == 0;
            assert sampleModel.getBitOffsets()[3] == 24;
            int[] rawInts = ((DataBufferInt) imageRaster.getDataBuffer()).getData();

            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX();

            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;

                int rowByteOffset = 1;
                for (int x = 0; x < width; x++) {
                    final int element = rawInts[pixelPtr++];
                    byte r = (byte) (element >> 16); // R
                    byte g = (byte) (element >> 8); // G
                    byte b = (byte) element; // B
                    byte a = (byte) (element >> 24); // A

                    if (preMultipliedAlpha) {
                        // If alpha is 0 or 255 we don't need to do anything
                        // (the color values are either 0 or we are dividing by 1)
                        if (a != 0 && a != -1) { // Java's bytes are unsigned, so 255 is represented by -1
                            double normalizedInverseAlpha = 1.0d / ((a & 0xff) / 255.0);
                            r = (byte) ((r & 0xff) * normalizedInverseAlpha + 0.5);
                            g = (byte) ((g & 0xff) * normalizedInverseAlpha + 0.5);
                            b = (byte) ((b & 0xff) * normalizedInverseAlpha + 0.5);
                        }
                    }

                    currLine[rowByteOffset++] = r;
                    currLine[rowByteOffset++] = g;
                    currLine[rowByteOffset++] = b;
                    currLine[rowByteOffset++] = a;
                }

                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);

                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
        } else {
            throw new IllegalStateException("TYPE_INT_RGB must have a SinglePixelPackedSampleModel");
        }
    }

    static void getIntBgr(WritableRaster imageRaster, int yStart, int width, int heightToStream,
            AbstractPNGLineConsumer consumer) throws IOException {
        final int channels = 3;
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        if (imageRaster.getSampleModel() instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sampleModel = (SinglePixelPackedSampleModel) imageRaster.getSampleModel();
            int scanlineStride = sampleModel.getScanlineStride();
            assert sampleModel.getNumBands() == 3;
            assert sampleModel.getBitOffsets()[0] == 0;
            assert sampleModel.getBitOffsets()[1] == 8;
            assert sampleModel.getBitOffsets()[2] == 16;
            int[] rawInts = ((DataBufferInt) imageRaster.getDataBuffer()).getData();

            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX();
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;

                int rowByteOffset = 1;
                for (int x = 0; x < width; x++) {
                    final int element = rawInts[pixelPtr++];
                    currLine[rowByteOffset++] = (byte) element; // R
                    currLine[rowByteOffset++] = (byte) (element >> 8); // G
                    currLine[rowByteOffset++] = (byte) (element >> 16); // B
                }

                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);

                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
        } else {
            throw new IllegalStateException("TYPE_INT_BGR must have a SinglePixelPackedSampleModel");
        }
    }

    static void get3ByteBgr(WritableRaster imageRaster, int yStart, int width, int heightToStream,
            AbstractPNGLineConsumer consumer) throws IOException {
        final int channels = 3;
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];
        DataBufferByte dataBufferByte = (DataBufferByte) imageRaster.getDataBuffer();
        if (imageRaster.getSampleModel() instanceof PixelInterleavedSampleModel) {
            PixelInterleavedSampleModel sampleModel = (PixelInterleavedSampleModel) imageRaster.getSampleModel();
            byte[] rawBytes = dataBufferByte.getData();
            int scanlineStride = sampleModel.getScanlineStride();
            int pixelStride = sampleModel.getPixelStride();

            assert pixelStride == 3;
            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX() * pixelStride;

            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;
                int writePtr = 1;
                for (int x = 0; x < width; x++) {
                    byte b = rawBytes[pixelPtr++];
                    byte g = rawBytes[pixelPtr++];
                    byte r = rawBytes[pixelPtr++];
                    currLine[writePtr++] = r;
                    currLine[writePtr++] = g;
                    currLine[writePtr++] = b;
                }
                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);
                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
        } else {
            throw new IllegalStateException("3ByteBgr must have a PixelInterleavedSampleModel");
        }
    }

    static void get4ByteAbgr(WritableRaster imageRaster, int yStart, int width, int heightToStream,
            boolean preMultipliedAlpha, AbstractPNGLineConsumer consumer) throws IOException {
        final int channels = 4;
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];
        DataBufferByte dataBufferByte = (DataBufferByte) imageRaster.getDataBuffer();
        if (imageRaster.getSampleModel() instanceof PixelInterleavedSampleModel) {
            PixelInterleavedSampleModel sampleModel = (PixelInterleavedSampleModel) imageRaster.getSampleModel();
            byte[] rawBytes = dataBufferByte.getData();
            int scanlineStride = sampleModel.getScanlineStride();
            int pixelStride = sampleModel.getPixelStride();

            assert pixelStride == 4;
            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX() * pixelStride;
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;
                int writePtr = 1;
                for (int x = 0; x < width; x++) {
                    byte a = rawBytes[pixelPtr++];
                    byte b = rawBytes[pixelPtr++];
                    byte g = rawBytes[pixelPtr++];
                    byte r = rawBytes[pixelPtr++];

                    if (preMultipliedAlpha) {
                        // If alpha is 0 or 255 we don't need to do anything
                        // (the color values are either 0 or we are dividing by 1)
                        if (a != 0 && a != -1) { // Java's bytes are unsigned, so 255 is represented by -1
                            double normalizedInverseAlpha = 1.0d / ((a & 0xff) / 255.0);
                            r = (byte) ((r & 0xff) * normalizedInverseAlpha + 0.5);
                            g = (byte) ((g & 0xff) * normalizedInverseAlpha + 0.5);
                            b = (byte) ((b & 0xff) * normalizedInverseAlpha + 0.5);
                        }
                    }

                    currLine[writePtr++] = r;
                    currLine[writePtr++] = g;
                    currLine[writePtr++] = b;
                    currLine[writePtr++] = a;
                }
                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);
                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
        } else {
            throw new IllegalStateException("4ByteAbgr must have a PixelInterleavedSampleModel");
        }
    }

    static void getByteGray(BufferedImage image, int yStart, int width, int heightToStream, AbstractPNGLineConsumer consumer)
            throws IOException {
        WritableRaster imageRaster = image.getRaster();

        final int channels = 1;
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        DataBufferByte dataBufferByte = (DataBufferByte) imageRaster.getDataBuffer();
        if (imageRaster.getSampleModel() instanceof PixelInterleavedSampleModel) {
            PixelInterleavedSampleModel sampleModel = (PixelInterleavedSampleModel) imageRaster.getSampleModel();
            byte[] rawBytes = dataBufferByte.getData();
            int scanlineStride = sampleModel.getScanlineStride();
            int pixelStride = sampleModel.getPixelStride();

            assert pixelStride == 1;
            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX() * pixelStride;
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;

                System.arraycopy(rawBytes, pixelPtr, currLine, 1, width);

                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);
                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
        } else {
            throw new IllegalStateException("TYPE_BYTE_GRAY must have a PixelInterleavedSampleModel");
        }
    }

    static void getUshortGray(BufferedImage image, int yStart, int width, int heightToStream, AbstractPNGLineConsumer consumer)
            throws IOException {
        WritableRaster imageRaster = image.getRaster();

        final int channels = 1;
        final int rowByteSize = 1 + channels * width * 2;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        DataBufferUShort dataBufferUShort = (DataBufferUShort) imageRaster.getDataBuffer();
        if (imageRaster.getSampleModel() instanceof PixelInterleavedSampleModel) {
            PixelInterleavedSampleModel sampleModel = (PixelInterleavedSampleModel) imageRaster.getSampleModel();
            short[] rawShorts = dataBufferUShort.getData();
            int scanlineStride = sampleModel.getScanlineStride();
            int pixelStride = sampleModel.getPixelStride();

            assert pixelStride == 1;
            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX() * pixelStride;
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;
                int writePtr = 1;
                for (int x = 0; x < width; x++) {
                    short grayColorValue = rawShorts[pixelPtr++];
                    byte high = (byte) (grayColorValue >> 8);
                    byte low = (byte) (grayColorValue & 0xff);
                    currLine[writePtr++] = high;
                    currLine[writePtr++] = low;
                }
                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);
                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
        } else {
            throw new IllegalStateException("TYPE_USHORT_GRAY must have a PixelInterleavedSampleModel");
        }
    }


    static boolean getUshortGenericDataBufferUShort(BufferedImage image, int yStart, int width, int heightToStream, AbstractPNGLineConsumer consumer)
            throws IOException {
        WritableRaster imageRaster = image.getRaster();

        DataBufferUShort dataBufferUShort = (DataBufferUShort) imageRaster.getDataBuffer();
        final int channels = imageRaster.getSampleModel().getNumBands();
        final int rowByteSize = 1 + channels * width * 2;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        if (imageRaster.getSampleModel() instanceof PixelInterleavedSampleModel) {
            PixelInterleavedSampleModel sampleModel = (PixelInterleavedSampleModel) imageRaster.getSampleModel();
            short[] rawShorts = dataBufferUShort.getData();
            int scanlineStride = sampleModel.getScanlineStride();
            int pixelStride = sampleModel.getPixelStride();
            assert pixelStride == channels;

            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX() * pixelStride;
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;
                int writePtr = 1;
                for (int x = 0; x < width; x++) {
                    for (int inPixelPtr = 0; inPixelPtr < channels; inPixelPtr++) {
                        short colorValue = rawShorts[pixelPtr++];
                        byte high = (byte) (colorValue >> 8);
                        byte low = (byte) (colorValue & 0xff);
                        currLine[writePtr++] = high;
                        currLine[writePtr++] = low;
                    }
                }
                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);
                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
            return true;
        }
        return false;
    }

    static boolean getUshortGeneric(BufferedImage image, int yStart, int width, int heightToStream, AbstractPNGLineConsumer consumer)
            throws IOException {
        WritableRaster imageRaster = image.getRaster();

        final int channels = imageRaster.getSampleModel().getNumBands();
        final int rowByteSize = 1 + channels * width * 2;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        if (imageRaster.getSampleModel() instanceof PixelInterleavedSampleModel) {
            PixelInterleavedSampleModel sampleModel = (PixelInterleavedSampleModel) imageRaster.getSampleModel();
            DataBuffer dataBuffer = imageRaster.getDataBuffer();
            int numBanks = dataBuffer.getNumBanks();
            int scanlineStride = sampleModel.getScanlineStride();
            int pixelStride = sampleModel.getPixelStride();
            assert pixelStride == channels;
            assert numBanks == 1;

            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX() * pixelStride;
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;
                int writePtr = 1;
                for (int x = 0; x < width; x++) {
                    for (int bankNum = 0; bankNum < channels; bankNum++) {
                        short colorValue = (short) (dataBuffer.getElem(pixelPtr++) & 0xFFFF);
                        byte high = (byte) (colorValue >> 8);
                        byte low = (byte) (colorValue & 0xff);
                        currLine[writePtr++] = high;
                        currLine[writePtr++] = low;
                    }
                }
                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);
                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
            return true;
        }
        return false;
    }

    static boolean getByteGeneric(BufferedImage image, int yStart, int width, int heightToStream, AbstractPNGLineConsumer consumer)
            throws IOException {
        WritableRaster imageRaster = image.getRaster();

        final int channels = imageRaster.getSampleModel().getNumBands();
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        if (imageRaster.getSampleModel() instanceof PixelInterleavedSampleModel) {
            PixelInterleavedSampleModel sampleModel = (PixelInterleavedSampleModel) imageRaster.getSampleModel();
            DataBuffer dataBuffer = imageRaster.getDataBuffer();
            int numBanks = dataBuffer.getNumBanks();
            int scanlineStride = sampleModel.getScanlineStride();
            int pixelStride = sampleModel.getPixelStride();
            assert pixelStride == channels;
            assert numBanks == 1;

            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX() * pixelStride;
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;
                int writePtr = 1;
                for (int x = 0; x < width; x++) {
                    for (int bankNum = 0; bankNum < channels; bankNum++) {
                        byte colorValue = (byte) (dataBuffer.getElem(pixelPtr++) & 0xFF);
                        currLine[writePtr++] = colorValue;
                    }
                }
                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);
                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
            return true;
        }
        return false;
    }


    static boolean getIntGeneric(BufferedImage image, int yStart, int width, int heightToStream, AbstractPNGLineConsumer consumer)
            throws IOException {
        WritableRaster imageRaster = image.getRaster();

        final int channels = imageRaster.getSampleModel().getNumBands();
        final int rowByteSize = 1 + channels * width;
        byte[] currLine = new byte[rowByteSize];
        byte[] prevLine = new byte[rowByteSize];

        if (imageRaster.getSampleModel() instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sampleModel = (SinglePixelPackedSampleModel) imageRaster.getSampleModel();
            DataBuffer dataBuffer = imageRaster.getDataBuffer();
            int numBanks = dataBuffer.getNumBanks();
            int scanlineStride = sampleModel.getScanlineStride();
            int[] bitOffsets = sampleModel.getBitOffsets();
            int[] bitMasks = sampleModel.getBitMasks();
            assert numBanks == 1;

            int linePtr = scanlineStride * (yStart - imageRaster.getSampleModelTranslateY())
                    - imageRaster.getSampleModelTranslateX();
            for (int y = 0; y < heightToStream; y++) {
                int pixelPtr = linePtr;
                int writePtr = 1;
                for (int x = 0; x < width; x++) {
                    int colorValue = dataBuffer.getElem(pixelPtr++);
                    for (int i = 0; i < bitOffsets.length; i++) {
                        int v = (colorValue & bitMasks[i]) >> bitOffsets[i];
                        currLine[writePtr++] = (byte) (v & 0xFF);
                    }
                }
                linePtr += scanlineStride;
                consumer.consume(currLine, prevLine);
                {
                    byte[] b = currLine;
                    currLine = prevLine;
                    prevLine = b;
                }
            }
            return true;
        }
        return false;
    }
}
