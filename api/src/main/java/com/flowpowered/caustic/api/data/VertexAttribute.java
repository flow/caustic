/*
 * This file is part of Caustic API, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Flow Powered <https://flowpowered.com/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.caustic.api.data;

import java.nio.ByteBuffer;

import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TShortIterator;
import gnu.trove.list.TByteList;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TShortList;

import com.flowpowered.caustic.api.util.CausticUtil;

/**
 * Represents a vertex attribute. It has a name, a data type, a size (the number of components) and data.
 */
public class VertexAttribute implements Cloneable {
    protected final String name;
    protected final DataType type;
    protected final int size;
    protected final UploadMode uploadMode;
    private ByteBuffer buffer;

    /**
     * Creates a new vertex attribute from the name, the data type and the size. The upload mode will be {@link UploadMode#TO_FLOAT}.
     *
     * @param name The name
     * @param type The type
     * @param size The size
     */
    public VertexAttribute(String name, DataType type, int size) {
        this(name, type, size, UploadMode.TO_FLOAT);
    }

    /**
     * Creates a new vertex attribute from the name, the data type, the size and the upload mode.
     *
     * @param name The name
     * @param type The type
     * @param size The size
     * @param uploadMode the upload mode
     */
    public VertexAttribute(String name, DataType type, int size, UploadMode uploadMode) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.uploadMode = uploadMode;
    }

    /**
     * Returns the name of the attribute.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data type of the attribute.
     *
     * @return The data type
     */
    public DataType getType() {
        return type;
    }

    /**
     * Return the size of the attribute.
     *
     * @return The size
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the upload mode for this attribute.
     *
     * @return The upload mode
     */
    public UploadMode getUploadMode() {
        return uploadMode;
    }

    /**
     * Returns a new byte buffer filled and ready to read, containing the attribute data. This method will {@link java.nio.ByteBuffer#flip()} the buffer before returning it.
     *
     * @return The buffer
     */
    public ByteBuffer getData() {
        if (this.buffer == null) {
            throw new IllegalStateException("ByteBuffer must have data before it is ready for use.");
        }
        final ByteBuffer copy = CausticUtil.createByteBuffer(buffer.capacity());
        buffer.rewind();
        copy.put(buffer);
        copy.flip();
        return copy;
    }

    /**
     * Replaces the current buffer data with a copy of the given {@link java.nio.ByteBuffer} This method arbitrarily creates data for the ByteBuffer regardless of the data type of the vertex
     * attribute.
     *
     * @param buffer to set
     */
    public void setData(ByteBuffer buffer) {
        buffer.rewind();
        this.buffer = CausticUtil.createByteBuffer(buffer.capacity());
        this.buffer.put(buffer);
    }

    /**
     * Replaces the current buffer data with the list of bytes in the give {@link gnu.trove.list.TByteList} This method arbitrarily creates data for the ByteBuffer regardless of the data type of the
     * vertex attribute.
     *
     * @param list to set
     */
    public void setData(TByteList list) {
        this.buffer = CausticUtil.createByteBuffer(list.size());
        this.buffer.put(list.toArray());
    }

    /**
     * Replaces the current buffer data with the list of bytes in the give {@link gnu.trove.list.TShortList} This method arbitrarily creates data for the ByteBuffer regardless of the data type of the
     * vertex attribute.
     *
     * @param list to set
     */
    public void setData(TShortList list) {
        this.buffer = CausticUtil.createByteBuffer(list.size() * DataType.SHORT.getByteSize());
        final TShortIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            buffer.putShort(iterator.next());
        }
    }

    /**
     * Replaces the current buffer data with the list of bytes in the give {@link gnu.trove.list.TIntList} This method arbitrarily creates data for the ByteBuffer regardless of the data type of the
     * vertex attribute.
     *
     * @param list to set
     */
    public void setData(TIntList list) {
        this.buffer = CausticUtil.createByteBuffer(list.size() * DataType.INT.getByteSize());
        final TIntIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            buffer.putInt(iterator.next());
        }
    }

    /**
     * Replaces the current buffer data with the list of bytes in the give {@link gnu.trove.list.TFloatList} This method arbitrarily creates data for the ByteBuffer regardless of the data type of the
     * vertex attribute.
     *
     * @param list to set
     */
    public void setData(TFloatList list) {
        this.buffer = CausticUtil.createByteBuffer(list.size() * DataType.FLOAT.getByteSize());
        final TFloatIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            buffer.putFloat(iterator.next());
        }
    }

    /**
     * Replaces the current buffer data with the list of bytes in the give {@link gnu.trove.list.TDoubleList} This method arbitrarily creates data for the ByteBuffer regardless of the data type of the
     * vertex attribute.
     *
     * @param list to set
     */
    public void setData(TDoubleList list) {
        this.buffer = CausticUtil.createByteBuffer(list.size() * DataType.DOUBLE.getByteSize());
        final TDoubleIterator iterator = list.iterator();
        while (iterator.hasNext()) {
            buffer.putDouble(iterator.next());
        }
    }

    /**
     * Clears all of the buffer data.
     */
    public void clearData() {
        if (buffer != null) {
            buffer.clear();
        }
    }

    @Override
    public VertexAttribute clone() {
        final VertexAttribute clone = new VertexAttribute(name, type, size, uploadMode);
        clone.setData(this.buffer);
        return clone;
    }

    /**
     * Represents an attribute data type.
     */
    public static enum DataType {
        BYTE(0x1400, 1, true, true), // GL11.GL_BYTE
        UNSIGNED_BYTE(0x1401, 1, true, false), // GL11.GL_UNSIGNED_BYTE
        SHORT(0x1402, 2, true, true), // GL11.GL_SHORT
        UNSIGNED_SHORT(0x1403, 2, true, false), // GL11.GL_UNSIGNED_SHORT
        INT(0x1404, 4, true, true), // GL11.GL_INT
        UNSIGNED_INT(0x1405, 4, true, false), // GL11.GL_UNSIGNED_INT
        HALF_FLOAT(0x140B, 2, false, true), // GL30.GL_HALF_FLOAT
        FLOAT(0x1406, 4, false, true), // GL11.GL_FLOAT
        DOUBLE(0x140A, 8, false, true); // GL11.GL_DOUBLE
        private final int glConstant;
        private final int byteSize;
        private final boolean integer;
        private final boolean signed;
        private final int multiplyShift;

        private DataType(int glConstant, int byteSize, boolean integer, boolean signed) {
            this.glConstant = glConstant;
            this.byteSize = byteSize;
            this.integer = integer;
            this.signed = signed;
            multiplyShift = (int) (Math.log(byteSize) / Math.log(2));
        }

        /**
         * Returns the OpenGL constant for the data type.
         *
         * @return The OpenGL constant
         */
        public int getGLConstant() {
            return glConstant;
        }

        /**
         * Returns the size in bytes of the data type.
         *
         * @return The size in bytes
         */
        public int getByteSize() {
            return byteSize;
        }

        /**
         * Returns true if the data type is an integer number ({@link DataType#BYTE}, {@link DataType#SHORT} or {@link DataType#INT}).
         *
         * @return Whether or not the data type is an integer
         */
        public boolean isInteger() {
            return integer;
        }

        /**
         * Returns true if this data type supports signed numbers, false if not.
         *
         * @return Whether or not this data type supports signed numbers
         */
        public boolean isSigned() {
            return signed;
        }

        /**
         * Returns the shift amount equivalent to multiplying by the number of bytes in this data type.
         *
         * @return The shift amount corresponding to the multiplication by the byte size
         */
        public int getMultiplyShift() {
            return multiplyShift;
        }
    }

    /**
     * The uploading mode. When uploading attribute data to OpenGL, integer data can be either converted to float or not (the later is only possible with version 3.0+). When converting to float, the
     * data can be normalized or not. By default, {@link UploadMode#TO_FLOAT} is used as it provides the best compatibility.
     */
    public static enum UploadMode {
        TO_FLOAT,
        TO_FLOAT_NORMALIZE,
        /**
         * Only supported in OpenGL 3.0 and after.
         */
        KEEP_INT;

        /**
         * Returns true if this upload mode converts integer data to normalized floats.
         *
         * @return Whether or not this upload mode converts integer data to normalized floats
         */
        public boolean normalize() {
            return this == TO_FLOAT_NORMALIZE;
        }

        /**
         * Returns true if this upload mode converts the data to floats.
         *
         * @return Whether or not this upload mode converts the data to floats
         */
        public boolean toFloat() {
            return this == TO_FLOAT || this == TO_FLOAT_NORMALIZE;
        }
    }
}
