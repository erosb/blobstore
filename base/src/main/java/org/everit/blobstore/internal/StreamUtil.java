/**
 * This file is part of Everit - Blobstore.
 *
 * Everit - Blobstore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This file is part of Everit - Blobstore Base.
 *
 * Everit - Blobstore Base is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Blobstore Base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Blobstore Base.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.blobstore.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Util class for stream data manipulation.
 */
public final class StreamUtil {

    /**
     * Copies the length of bytes from the inputstream to the outputStream.
     * 
     * @param is
     *            The inputstream where the bytes will be copied from.
     * @param out
     *            The outputStream where the bytes will be copied to.
     * @param amount
     *            The amount of bytes that will be copied. If not given the inputstream will be read till the end.
     * @param bufferSize
     *            The size of the buffer which the copy will be done with.
     * @throws IOException
     *             if an error occurs on the output or input stream.
     * @return The number of bytes that were copied.
     */
    public static long copyStream(
            final InputStream is, final OutputStream out, final Long amount, final int bufferSize) throws IOException {
        long bytesProcessed = 0;
        byte[] buf = new byte[bufferSize];
        int bytesReadOut = 0;
        while (((amount == null) || (bytesProcessed < amount.longValue())) && (bytesReadOut > -1)) {
            int bytesToRead = bufferSize;
            if (amount != null) {
                long bytesLeft = amount.longValue() - bytesProcessed;
                if ((bytesLeft < bytesToRead)) {
                    bytesToRead = (int) (bytesLeft);
                }
            }
            bytesReadOut = is.read(buf, 0, bytesToRead);
            if (bytesReadOut > -1) {
                bytesProcessed = bytesProcessed + bytesReadOut;
                out.write(buf, 0, bytesReadOut);
            }
        }
        return bytesProcessed;
    }

    public static long countBytesProcessed(
            final InputStream is, final long length, final int bufferSize) throws IOException {
        long bytesProcessed = 0;
        byte[] buf = new byte[bufferSize];
        int bytesReadOut = 0;
        if (is.markSupported()) {
            is.mark(Long.valueOf(length).intValue() + bufferSize);
        }
        while ((bytesProcessed < length) && (bytesReadOut > -1)) {
            int bytesToRead = bufferSize;
            long bytesLeft = length - bytesProcessed;
            if ((bytesLeft < bytesToRead)) {
                bytesToRead = (int) (bytesLeft);
            }
            bytesReadOut = is.read(buf, 0, bytesToRead);
            if (bytesReadOut > -1) {
                bytesProcessed = bytesProcessed + bytesReadOut;
            }
        }
        if (is.markSupported()) {
            is.reset();
        }
        return bytesProcessed;
    }

    /**
     * Skip a number of bytes in an input stream.
     * 
     * @param in
     *            the input stream
     * @param skip
     *            the number of bytes to skip
     * @throws IOException
     *             if an IO exception occurred while skipping. In case the stream is not long enough an
     *             {@link EOFException} is thrown.
     */
    public static void skip(final InputStream in, final long skip) throws IOException {

        long bytesLeft = skip;

        while (bytesLeft > 0) {
            long skipped = in.skip(bytesLeft);
            if (skipped <= 0) {
                throw new EOFException();
            }
            bytesLeft = bytesLeft - skipped;
        }

    }

    /**
     * Private constructor for superclass.
     */
    private StreamUtil() {
    }
}
