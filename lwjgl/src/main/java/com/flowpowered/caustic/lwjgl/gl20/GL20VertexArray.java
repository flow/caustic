/*
 * This file is part of Caustic LWJGL, licensed under the MIT License (MIT).
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
package com.flowpowered.caustic.lwjgl.gl20;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.APPLEVertexArrayObject;
import org.lwjgl.opengl.ARBVertexArrayObject;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

import com.flowpowered.caustic.api.data.VertexAttribute;
import com.flowpowered.caustic.api.data.VertexAttribute.DataType;
import com.flowpowered.caustic.api.data.VertexData;
import com.flowpowered.caustic.api.gl.VertexArray;
import com.flowpowered.caustic.lwjgl.LWJGLUtil;

/**
 * An OpenGL 2.0 implementation of {@link VertexArray}.
 * <p/>
 * Vertex arrays will be used if the ARB or APPLE extension is supported by the hardware. Else, since core OpenGL doesn't support them until 3.0, the vertex attributes will have to be redefined on
 * each render call.
 *
 * @see VertexArray
 */
public class GL20VertexArray extends VertexArray {
    private static final int[] EMPTY_ARRAY = {};
    // Buffers IDs
    private int indicesBufferID = 0;
    private int[] attributeBufferIDs = EMPTY_ARRAY;
    // Size of the attribute buffers
    private int[] attributeBufferSizes = EMPTY_ARRAY;
    // Amount of indices to render
    private int indicesCount = 0;
    private int indicesDrawCount = 0;
    // First and last index to render
    private int indicesOffset = 0;
    // Drawing mode
    private DrawingMode drawingMode = DrawingMode.TRIANGLES;
    // Polygon mode
    private PolygonMode polygonMode = PolygonMode.FILL;
    // The available vao extension
    private final VertexArrayExtension extension;
    // Attribute properties for when we don't have a vao extension
    private int[] attributeSizes;
    private int[] attributeTypes;
    private boolean[] attributeNormalizing;

    public GL20VertexArray() {
        final ContextCapabilities capabilities = GLContext.getCapabilities();
        if (capabilities.GL_ARB_vertex_array_object) {
            extension = VertexArrayExtension.ARB;
        } else if (capabilities.GL_APPLE_vertex_array_object) {
            extension = VertexArrayExtension.APPLE;
        } else {
            extension = VertexArrayExtension.NONE;
        }
    }

    @Override
    public void create() {
        checkNotCreated();
        if (extension.has()) {
            // Generate the vao
            id = extension.glGenVertexArrays();
        }
        // Update state
        super.create();
        // Check for errors
        LWJGLUtil.checkForGLError();
    }

    @Override
    public void destroy() {
        checkCreated();
        // Delete the indices buffer
        GL15.glDeleteBuffers(indicesBufferID);
        // Delete the attribute buffers
        for (int attributeBufferID : attributeBufferIDs) {
            GL15.glDeleteBuffers(attributeBufferID);
        }
        if (extension.has()) {
            // Delete the vao
            extension.glDeleteVertexArrays(id);
        } else {
            // Else delete the attribute properties
            attributeSizes = null;
            attributeTypes = null;
            attributeNormalizing = null;
        }
        // Reset the IDs and data
        indicesBufferID = 0;
        attributeBufferIDs = EMPTY_ARRAY;
        attributeBufferSizes = EMPTY_ARRAY;
        // Update the state
        super.destroy();
        // Check for errors
        LWJGLUtil.checkForGLError();
    }

    @Override
    public void setData(VertexData vertexData) {
        checkCreated();
        // Generate a new indices buffer if we don't have one yet
        if (indicesBufferID == 0) {
            indicesBufferID = GL15.glGenBuffers();
        }
        // Bind the indices buffer
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBufferID);
        // Get the new count of indices
        final int newIndicesCount = vertexData.getIndicesCount();
        // If the new count is greater than or 50% smaller than the old one, we'll reallocate the memory
        // In the first case because we need more space, in the other to save space
        if (newIndicesCount > indicesCount || newIndicesCount <= indicesCount * 0.5) {
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, vertexData.getIndicesBuffer(), GL15.GL_STATIC_DRAW);
        } else {
            // Else, we replace the data with the new one, but we don't resize, so some old data might be left trailing in the buffer
            GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, vertexData.getIndicesBuffer());
        }
        // Unbind the indices buffer
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Update the total indices count
        indicesCount = newIndicesCount;
        // Ensure the count fits under the total one
        indicesDrawCount = indicesDrawCount <= 0 ? indicesCount : Math.min(indicesDrawCount, indicesCount);
        // Ensure that the indices offset and count fits inside the valid part of the buffer
        indicesOffset = Math.min(indicesOffset, indicesDrawCount - 1);
        indicesDrawCount -= indicesOffset;
        // Bind the vao
        if (extension.has()) {
            extension.glBindVertexArray(id);
        }
        // Create a new array of attribute buffers ID of the correct size
        final int attributeCount = vertexData.getAttributeCount();
        final int[] newAttributeBufferIDs = new int[attributeCount];
        // Copy all the old buffer IDs that will fit in the new array so we can reuse them
        System.arraycopy(attributeBufferIDs, 0, newAttributeBufferIDs, 0, Math.min(attributeBufferIDs.length, newAttributeBufferIDs.length));
        // Delete any buffers that we don't need (new array is smaller than the previous one)
        for (int i = newAttributeBufferIDs.length; i < attributeBufferIDs.length; i++) {
            GL15.glDeleteBuffers(attributeBufferIDs[i]);
        }
        // Create new buffers if necessary (new array is larger than the previous one)
        for (int i = attributeBufferIDs.length; i < newAttributeBufferIDs.length; i++) {
            newAttributeBufferIDs[i] = GL15.glGenBuffers();
        }
        // Copy the old valid attribute buffer sizes
        final int[] newAttributeBufferSizes = new int[attributeCount];
        System.arraycopy(attributeBufferSizes, 0, newAttributeBufferSizes, 0, Math.min(attributeBufferSizes.length, newAttributeBufferSizes.length));
        // If we don't have a vao, we have to save the properties manually
        if (!extension.has()) {
            attributeSizes = new int[attributeCount];
            attributeTypes = new int[attributeCount];
            attributeNormalizing = new boolean[attributeCount];
        }
        // Upload the new vertex data
        for (int i = 0; i < attributeCount; i++) {
            final VertexAttribute attribute = vertexData.getAttribute(i);
            final ByteBuffer attributeData = attribute.getData();
            // Get the current buffer size
            final int bufferSize = newAttributeBufferSizes[i];
            // Get the new buffer size
            final int newBufferSize = attributeData.remaining();
            // Bind the target buffer
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, newAttributeBufferIDs[i]);
            // If the new count is greater than or 50% smaller than the old one, we'll reallocate the memory
            if (newBufferSize > bufferSize || newBufferSize <= bufferSize * 0.5) {
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, attributeData, GL15.GL_STATIC_DRAW);
            } else {
                // Else, we replace the data with the new one, but we don't resize, so some old data might be left trailing in the buffer
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, attributeData);
            }
            // Update the buffer size to the new one
            newAttributeBufferSizes[i] = newBufferSize;
            // Next, we add the pointer to the data in the vao
            if (extension.has()) {
                // As a float, normalized or not
                GL20.glVertexAttribPointer(i, attribute.getSize(), attribute.getType().getGLConstant(), attribute.getUploadMode().normalize(), 0, 0);
                // Enable the attribute
                GL20.glEnableVertexAttribArray(i);
            } else {
                // Else we save the properties for rendering
                attributeSizes[i] = attribute.getSize();
                attributeTypes[i] = attribute.getType().getGLConstant();
                attributeNormalizing[i] = attribute.getUploadMode().normalize();
            }
        }
        // Unbind the last vbo
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        // Unbind the vao
        if (extension.has()) {
            extension.glBindVertexArray(0);
        }
        // Update the attribute buffer IDs to the new ones
        attributeBufferIDs = newAttributeBufferIDs;
        // Update the attribute buffer sizes to the new ones
        attributeBufferSizes = newAttributeBufferSizes;
        // Check for errors
        LWJGLUtil.checkForGLError();
    }

    @Override
    public void setDrawingMode(DrawingMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Drawing mode cannot be null");
        }
        this.drawingMode = mode;
    }

    @Override
    public void setPolygonMode(PolygonMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Polygon mode cannot be null");
        }
        polygonMode = mode;
    }

    @Override
    public void setIndicesOffset(int offset) {
        indicesOffset = Math.min(offset, indicesCount - 1);
        indicesDrawCount = Math.min(indicesDrawCount, indicesCount - indicesOffset);
    }

    @Override
    public void setIndicesCount(int count) {
        indicesDrawCount = count <= 0 ? indicesCount : count;
        indicesDrawCount = Math.min(count, indicesCount - indicesOffset);
    }

    @Override
    public void draw() {
        checkCreated();
        if (extension.has()) {
            // Bind the vao
            extension.glBindVertexArray(id);
        } else {
            // Enable the vertex attributes
            for (int i = 0; i < attributeBufferIDs.length; i++) {
                // Bind the buffer
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, attributeBufferIDs[i]);
                // Define the attribute
                GL20.glVertexAttribPointer(i, attributeSizes[i], attributeTypes[i], attributeNormalizing[i], 0, 0);
                // Enable it
                GL20.glEnableVertexAttribArray(i);
            }
            // Unbind the last buffer
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }
        // Bind the index buffer
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBufferID);
        // Set the polygon mode
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, polygonMode.getGLConstant());
        // Draw all indices with the provided mode
        GL11.glDrawElements(drawingMode.getGLConstant(), indicesDrawCount, GL11.GL_UNSIGNED_INT, indicesOffset * DataType.INT.getByteSize());
        // Unbind the indices buffer
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Check for errors
        LWJGLUtil.checkForGLError();
    }

    @Override
    public GLVersion getGLVersion() {
        return GLVersion.GL20;
    }

    private static enum VertexArrayExtension {
        NONE,
        ARB,
        APPLE;

        private boolean has() {
            return this != NONE;
        }

        private int glGenVertexArrays() {
            switch (this) {
                case ARB:
                    return ARBVertexArrayObject.glGenVertexArrays();
                case APPLE:
                    return APPLEVertexArrayObject.glGenVertexArraysAPPLE();
                default:
                    return 0;
            }
        }

        private void glBindVertexArray(int array) {
            switch (this) {
                case ARB:
                    ARBVertexArrayObject.glBindVertexArray(array);
                    break;
                case APPLE:
                    APPLEVertexArrayObject.glBindVertexArrayAPPLE(array);
            }
        }

        private void glDeleteVertexArrays(int array) {
            switch (this) {
                case ARB:
                    ARBVertexArrayObject.glDeleteVertexArrays(array);
                    break;
                case APPLE:
                    APPLEVertexArrayObject.glDeleteVertexArraysAPPLE(array);
            }
        }
    }
}
