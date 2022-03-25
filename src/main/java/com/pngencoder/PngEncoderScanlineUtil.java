package com.pngencoder;

import java.awt.*;
import java.awt.image.*;

class PngEncoderScanlineUtil {
	private PngEncoderScanlineUtil() {
	}

	static byte[] get(BufferedImage bufferedImage) {
		final int width = bufferedImage.getWidth();
		final int height = bufferedImage.getHeight();

		final PngEncoderBufferedImageType type = PngEncoderBufferedImageType.valueOf(bufferedImage);

		WritableRaster raster = bufferedImage.getRaster();
		if (type == PngEncoderBufferedImageType.TYPE_INT_RGB) {
			return getIntRgb(raster, width, height);
		}

		if (type == PngEncoderBufferedImageType.TYPE_INT_ARGB) {
			return getIntArgb(raster, width, height);
		}

		// TODO: TYPE_INT_ARGB_PRE

		if (type == PngEncoderBufferedImageType.TYPE_INT_BGR) {
			return getIntRgb(raster, width, height);
		}

		if (type == PngEncoderBufferedImageType.TYPE_3BYTE_BGR) {
			return get3ByteBgr(raster, width, height);
		}

		if (type == PngEncoderBufferedImageType.TYPE_4BYTE_ABGR) {
			return get4ByteAbgr(raster, width, height);
		}

		// TODO: TYPE_4BYTE_ABGR_PRE

		// TODO: TYPE_USHORT_565_RGB
		// TODO: TYPE_USHORT_555_RGB

		if (type == PngEncoderBufferedImageType.TYPE_BYTE_GRAY) {
			return getByteGray(raster, width, height);
		}

		if (type == PngEncoderBufferedImageType.TYPE_USHORT_GRAY) {
			return getUshortGray(raster, width, height);
		}

		// Fallback for unsupported type.
		final int[] elements = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
		if (bufferedImage.getTransparency() == Transparency.OPAQUE) {
			return getIntRgb(elements, width, height);
		} else {
			return getIntArgb(elements, width, height);
		}
	}

	static byte[] getIntRgb(int[] elements, int width, int height) {
		final int channels = 3;
		final int rowByteSize = 1 + channels * width;
		final byte[] bytes = new byte[rowByteSize * height];
		for (int y = 0; y < height; y++) {
			int yOffset = y * width;
			int yRowBytesOffset = y * rowByteSize;

			for (int x = 0; x < width; x++) {
				final int element = elements[yOffset + x];
				int rowByteOffset = 1 + yRowBytesOffset + x * channels;
				bytes[rowByteOffset] = (byte) ((element >> 16) & 0xFF); // R
				bytes[rowByteOffset + 1] = (byte) ((element >> 8) & 0xFF); // G
				bytes[rowByteOffset + 2] = (byte) ((element) & 0xFF); // B
			}
		}
		return bytes;
	}

	static byte[] getIntArgb(int[] elements, int width, int height) {
		final int channels = 4;
		final int rowByteSize = 1 + channels * width;
		final byte[] bytes = new byte[rowByteSize * height];
		for (int y = 0; y < height; y++) {
			int yOffset = y * width;

			int yRowBytesOffset = y * rowByteSize;

			for (int x = 0; x < width; x++) {
				final int element = elements[yOffset + x];
				int rowByteOffset = 1 + yRowBytesOffset + x * channels;
				bytes[rowByteOffset] = (byte) ((element >> 16) & 0xFF); // R
				bytes[rowByteOffset + 1] = (byte) ((element >> 8) & 0xFF); // G
				bytes[rowByteOffset + 2] = (byte) ((element) & 0xFF); // B
				bytes[rowByteOffset + 3] = (byte) ((element >> 24) & 0xFF); // A
			}
		}
		return bytes;
	}

	static byte[] getIntRgb(WritableRaster imageRaster, int width, int height) {
		final int channels = 3;
		final int rowByteSize = 1 + channels * width;
		final byte[] bytes = new byte[rowByteSize * height];
		final int[] elements = new int[width];
		for (int y = 0; y < height; y++) {
			imageRaster.getDataElements(0, y, width, 1, elements);
			int yRowBytesOffset = y * rowByteSize;

			for (int x = 0; x < width; x++) {
				final int element = elements[x];
				int rowByteOffset = 1 + yRowBytesOffset + x * channels;
				bytes[rowByteOffset] = (byte) ((element >> 16) & 0xFF); // R
				bytes[rowByteOffset + 1] = (byte) ((element >> 8) & 0xFF); // G
				bytes[rowByteOffset + 2] = (byte) ((element) & 0xFF); // B
			}
		}
		return bytes;
	}

	static byte[] getIntArgb(WritableRaster imageRaster, int width, int height) {
		final int channels = 4;
		final int rowByteSize = 1 + channels * width;
		final byte[] bytes = new byte[rowByteSize * height];
		final int[] elements = new int[width];
		for (int y = 0; y < height; y++) {
			imageRaster.getDataElements(0, y, width, 1, elements);
			int yRowBytesOffset = y * rowByteSize;

			for (int x = 0; x < width; x++) {
				final int element = elements[x];
				int rowByteOffset = 1 + yRowBytesOffset + x * channels;
				bytes[rowByteOffset] = (byte) ((element >> 16) & 0xFF); // R
				bytes[rowByteOffset + 1] = (byte) ((element >> 8) & 0xFF); // G
				bytes[rowByteOffset + 2] = (byte) ((element) & 0xFF); // B
				bytes[rowByteOffset + 3] = (byte) ((element >> 24) & 0xFF); // A
			}
		}
		return bytes;
	}

	static byte[] get3ByteBgr(WritableRaster imageRaster, int width, int height) {
		final int channels = 3;
		final int rowByteSize = 1 + channels * width;
		final byte[] bytes = new byte[rowByteSize * height];
		final byte[] elements = new byte[width * 3];
		for (int y = 0; y < height; y++) {
			imageRaster.getDataElements(0, y, width, 1, elements);
			int yRowBytesOffset = y * rowByteSize;

			for (int x = 0; x < width; x++) {
				int xOffset = (x * 3);
				int rowByteOffset = 1 + yRowBytesOffset + x * channels;
				bytes[rowByteOffset] = elements[xOffset]; // R
				bytes[rowByteOffset + 1] = elements[xOffset + 1]; // G
				bytes[rowByteOffset + 2] = elements[xOffset + 2]; // B
			}
		}
		return bytes;
	}

	static byte[] get4ByteAbgr(WritableRaster imageRaster, int width, int height) {
		final int channels = 4;
		final int rowByteSize = 1 + channels * width;
		final byte[] elements = new byte[width * 4];
		final byte[] bytes = new byte[rowByteSize * height];
		for (int y = 0; y < height; y++) {
			imageRaster.getDataElements(0, y, width, 1, elements);
			int yRowBytesOffset = y * rowByteSize;

			for (int x = 0; x < width; x++) {
				int xOffset = x * 4;
				int rowByteOffset = 1 + yRowBytesOffset + x * channels;
				bytes[rowByteOffset] = elements[xOffset]; // R
				bytes[rowByteOffset + 1] = elements[xOffset + 1]; // G
				bytes[rowByteOffset + 2] = elements[xOffset + 2]; // B
				bytes[rowByteOffset + 3] = elements[xOffset + 3]; // A
			}
		}
		return bytes;
	}

	static byte[] getByteGray(WritableRaster imageRaster, int width, int height) {
		final int channels = 3;
		final int rowByteSize = 1 + channels * width;
		final byte[] bytes = new byte[rowByteSize * height];
		final byte[] elements = new byte[width];
		for (int y = 0; y < height; y++) {
			imageRaster.getDataElements(0, y, width, 1, elements);
			int yRowBytesOffset = y * rowByteSize;

			for (int x = 0; x < width; x++) {
				byte grayColorValue = elements[x];
				int rowByteOffset = 1 + yRowBytesOffset + x * channels;
				bytes[rowByteOffset] = grayColorValue; // R
				bytes[rowByteOffset + 1] = grayColorValue; // G
				bytes[rowByteOffset + 2] = grayColorValue; // B
			}
		}
		return bytes;
	}

	static byte[] getUshortGray(WritableRaster imageRaster, int width, int height) {

		final int channels = 3;
		final int rowByteSize = 1 + channels * width;
		final byte[] bytes = new byte[rowByteSize * height];
		final short[] elements = new short[width];

		for (int y = 0; y < height; y++) {
			int yRowBytesOffset = y * rowByteSize;
			imageRaster.getDataElements(0, y, width, 1, elements);

			for (int x = 0; x < width; x++) {
				byte grayColorValue = (byte) (elements[x] >> 8);
				int rowByteOffset = 1 + yRowBytesOffset + x * channels;

				bytes[rowByteOffset] = grayColorValue; // R
				bytes[rowByteOffset + 1] = grayColorValue; // G
				bytes[rowByteOffset + 2] = grayColorValue; // B
			}
		}
		return bytes;
	}
}
