package alchemystar.freedom.util;


/**
 * The type Buffer wrapper.
 * <p>
 * Buffer提供了write 和 read byte，Int,String,long等功能
 */
public class BufferWrapper {

    private byte[] buffer;

    // 写索引
    private int writeIndex;
    // 读索引
    private int readIndex;

    private int length;

    /**
     * Instantiates a new Buffer wrapper.
     *
     * @param size the size
     */
    public BufferWrapper(int size) {
        buffer = new byte[size];
        writeIndex = 0;
        readIndex = 0;
        length = buffer.length;
    }

    /**
     * Instantiates a new Buffer wrapper.
     *
     * @param buffer the buffer
     */
    public BufferWrapper(byte[] buffer) {
        this.buffer = buffer;
        writeIndex = 0;
        readIndex = 0;
        length = buffer.length;
    }

    /**
     * Get buffer byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Write int.
     *
     * @param i the
     */
    public void writeInt(int i) {
        buffer[writeIndex++] = (byte) (i & 0xff);
        buffer[writeIndex++] = (byte) (i >>> 8);
        buffer[writeIndex++] = (byte) (i >>> 16);
        buffer[writeIndex++] = (byte) (i >>> 24);
    }

    /**
     * Write int pos.
     *
     * @param i        the
     * @param position the position
     */
// 在指定位置写入int
    public void writeIntPos(int i, int position) {
        buffer[position++] = (byte) (i & 0xff);
        buffer[position++] = (byte) (i >>> 8);
        buffer[position++] = (byte) (i >>> 16);
        buffer[position++] = (byte) (i >>> 24);
    }

    /**
     * Write byte.
     *
     * @param i the
     */
    public void writeByte(byte i) {
        buffer[writeIndex++] = i;
    }

    /**
     * Write long.
     *
     * @param l the l
     */
    public void writeLong(long l) {
        buffer[writeIndex++] = (byte) (l & 0xff);
        buffer[writeIndex++] = (byte) (l >>> 8);
        buffer[writeIndex++] = (byte) (l >>> 16);
        buffer[writeIndex++] = (byte) (l >>> 24);
        buffer[writeIndex++] = (byte) (l >>> 32);
        buffer[writeIndex++] = (byte) (l >>> 40);
        buffer[writeIndex++] = (byte) (l >>> 48);
        buffer[writeIndex++] = (byte) (l >>> 56);
    }

    /**
     * Write string with null.
     *
     * @param s the s
     */
    public void writeStringWithNull(String s) {
        writeWithNull(s.getBytes());
    }

    /**
     * Write bytes.
     *
     * @param src the src
     */
    public void writeBytes(byte[] src) {
        System.arraycopy(src, 0, buffer, writeIndex, src.length);
    }

    /**
     * Write bytes.
     *
     * @param src      the src
     * @param position the position
     */
    public void writeBytes(byte[] src, int position) {
        System.arraycopy(src, 0, buffer, position, src.length);
    }

    /**
     * Write with null.
     *
     * @param src the src
     */
    public void writeWithNull(byte[] src) {
        System.arraycopy(src, 0, buffer, writeIndex, src.length);
        writeIndex += src.length;
        // 写入0
        buffer[writeIndex] = 0;
        writeIndex++;
    }

    /**
     * Read int int.
     *
     * @return the int
     */
    public int readInt() {
        final byte[] b = this.buffer;
        int i = b[readIndex++] & 0xff;
        i |= (b[readIndex++] & 0xff) << 8;
        i |= (b[readIndex++] & 0xff) << 16;
        i |= (b[readIndex++] & 0xff) << 24;
        return i;
    }

    /**
     * Read int pos int.
     *
     * @param position the position
     * @return the int
     */
    public int readIntPos(int position) {
        final byte[] b = this.buffer;
        int i = b[position++] & 0xff;
        i |= (b[position++] & 0xff) << 8;
        i |= (b[position++] & 0xff) << 16;
        i |= (b[position++] & 0xff) << 24;
        return i;
    }

    /**
     * Read long long.
     *
     * @return the long
     */
    public long readLong() {
        final byte[] b = this.buffer;
        long l = (long) (b[readIndex++] & 0xff);
        l |= (long) (b[readIndex++] & 0xff) << 8;
        l |= (long) (b[readIndex++] & 0xff) << 16;
        l |= (long) (b[readIndex++] & 0xff) << 24;
        l |= (long) (b[readIndex++] & 0xff) << 32;
        l |= (long) (b[readIndex++] & 0xff) << 40;
        l |= (long) (b[readIndex++] & 0xff) << 48;
        l |= (long) (b[readIndex++] & 0xff) << 56;
        return l;
    }

    /**
     * Read string with null string.
     *
     * @return the string
     */
    public String readStringWithNull() {
        return new String(readBytesWithNull());
    }

    /**
     * Read string with length string.
     *
     * @param position the position
     * @return the string
     */
    public String readStringWithLength(int position) {
        return new String(readBytesWithLength(position));
    }

    /**
     * Read byte byte.
     *
     * @return the byte
     */
    public byte readByte() {
        return buffer[readIndex++];
    }

    /**
     * Read bytes byte [ ].
     *
     * @param length the length
     * @return the byte [ ]
     */
// 读取当前位置指定长度的length
    public byte[] readBytes(int length) {
        final byte[] b = this.buffer;
        byte[] result = new byte[length];
        // 拷贝原数据到新的byte中
        System.arraycopy(b, readIndex, result, 0, length);
        readIndex += length;
        return result;
    }

    /**
     * Read bytes byte [ ].
     *
     * @param position the position
     * @param length   the length
     * @return the byte [ ]
     */
// 读取指定位置指定长度的length
    public byte[] readBytes(int position, int length) {
        final byte[] b = this.buffer;
        byte[] result = new byte[length];
        // 拷贝原数据到新的byte中
        System.arraycopy(b, position, result, 0, length);
        return result;
    }

    /**
     * Read bytes with length byte [ ].
     *
     * @param position the position
     * @return the byte [ ]
     */
// 读取指定地点的bytes
    public byte[] readBytesWithLength(int position) {
        final byte[] b = this.buffer;
        // 首先读取长度
        if (length - position < 4) {
            return null;
        }
        int length = readIntPos(position);
        // 跳过长度内容
        position += 4;
        // 然后读取内容
        if (length - position < length) {
            return null;
        }
        byte[] result = new byte[length];
        // 拷贝原数据到新的byte中
        System.arraycopy(b, position, result, 0, length);
        return result;
    }

    /**
     * Read bytes with null byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] readBytesWithNull() {
        final byte[] b = this.buffer;
        if (readIndex >= length) {
            return null;
        }
        int offset = -1;
        for (int i = readIndex; i < length; i++) {
            if (b[i] == 0) {
                offset = i;
                break;
            }
        }
        switch (offset) {
            case -1:
                byte[] ab1 = new byte[length - readIndex];
                System.arraycopy(b, readIndex, ab1, 0, ab1.length);
                readIndex = length;
                return ab1;
            case 0:
                readIndex++;
                return null;
            default:
                byte[] ab2 = new byte[offset - readIndex];
                System.arraycopy(b, readIndex, ab2, 0, ab2.length);
                readIndex = offset + 1;
                return ab2;
        }
    }

    /**
     * Write string length.
     *
     * @param s the s
     */
    public void writeStringLength(String s) {
        writeWithLength(s.getBytes());
    }

    /**
     * Write with length.
     *
     * @param src the src
     */
    public void writeWithLength(byte[] src) {
        writeInt(src.length);
        writeBytes(src);
    }

    /**
     * Remaining int.
     *
     * @return the int
     */
    public int remaining() {
        return length - readIndex;
    }

    /**
     * Clean.
     */
    public void clean() {
        // 重置索引
        writeIndex = 0;
        readIndex = 0;
        // 对未清0的数据需要小心
        // todo remvoe it
        buffer = new byte[buffer.length];
    }

    /**
     * Gets length.
     *
     * @return the length
     */
    public int getLength() {
        return length;
    }
}
