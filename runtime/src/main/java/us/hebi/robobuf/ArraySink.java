package us.hebi.robobuf;

import java.io.IOException;

/**
 * @author Florian Enner
 * @since 16 Aug 2019
 */
class ArraySink extends ProtoSink {

    ArraySink(final byte[] buffer,
              final int offset,
              final int length) {
        this.buffer = buffer;
        this.offset = offset;
        this.limit = offset + length;
        this.position = offset;
    }

    private int position;
    private byte[] buffer;
    private int offset;
    private int limit;

    public int position() {
        // This used to return ByteBuffer.position(), which is
        // the number of written bytes, and not the index within
        // the array.
        return position - offset;
    }

    public int spaceLeft() {
        return limit - position;
    }

    /**
     * Resets the position within the internal buffer to zero.
     *
     * @see #position
     * @see #spaceLeft
     */
    public void reset() {
        position = offset;
    }

    /** Write a single byte. */
    public void writeRawByte(final byte value) throws IOException {
        if (position >= limit) {
            // We're writing to a single buffer.
            throw new OutOfSpaceException(position, limit);
        }

        buffer[position++] = value;
    }

    /** Write part of an array of bytes. */
    public void writeRawBytes(final byte[] value, int offset, int length) throws IOException {
        if (spaceLeft() >= length) {
            // We have room in the current buffer.
            System.arraycopy(value, offset, buffer, position, length);
            position += length;
        } else {
            // We're writing to a single buffer.
            throw new OutOfSpaceException(position, limit);
        }
    }

    @Override
    public void writeStringNoTag(final CharSequence value) throws IOException {
        // UTF-8 byte length of the string is at least its UTF-16 code unit length (value.length()),
        // and at most 3 times of it. Optimize for the case where we know this length results in a
        // constant varint length - saves measuring length of the string.
        try {
            final int minLengthVarIntSize = computeRawVarint32Size(value.length());
            final int maxLengthVarIntSize = computeRawVarint32Size(value.length() * MAX_UTF8_EXPANSION);
            if (minLengthVarIntSize == maxLengthVarIntSize) {
                int startPosition = position + minLengthVarIntSize;
                int endPosition = encode(value, buffer, startPosition, spaceLeft());
                writeRawVarint32(endPosition - startPosition);
                position = endPosition;
            } else {
                writeRawVarint32(encodedLength(value));
                position = encode(value, buffer, position, spaceLeft());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            final OutOfSpaceException outOfSpaceException = new OutOfSpaceException(position, limit);
            outOfSpaceException.initCause(e);
            throw outOfSpaceException;
        }
    }

}
