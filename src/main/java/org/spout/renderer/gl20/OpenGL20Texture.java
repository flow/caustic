/*
 * This file is part of Caustic.
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 * Caustic is licensed under the Spout License Version 1.
 *
 * Caustic is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Caustic is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spout.renderer.gl20;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.GLU;

import org.spout.renderer.gl.GLVersion;
import org.spout.renderer.gl.Texture;
import org.spout.renderer.util.RenderUtil;

/**
 * An OpenGL 2.0 implementation of {@link Texture}.
 *
 * @see Texture
 */
public class OpenGL20Texture extends Texture {
	@Override
	public void create() {
		// Get the context capabilities for the graphics hardware
		//final ContextCapabilities contextCaps = GLContext.getCapabilities();
		//if (!contextCaps.GL_ARB_texture_non_power_of_two && (!GenericMath.isPowerOfTwo(width) || !GenericMath.isPowerOfTwo(height))) {
		// TODO: Resize images. Also, this only really matters for mipmaps
		//}
		// Generate and bind the texture in the unit
		id = GL11.glGenTextures();
		GL13.glActiveTexture(unit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		// Set pixel storage mode
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		// Upload the texture to the GPU
		uploadTexture(imageData, width, height);
		// Set the vertical and horizontal texture wraps (in the texture parameters)
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapT.getGLConstant());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapS.getGLConstant());
		// Set the min and max texture filters (in the texture parameters)
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter.getGLConstant());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter.getGLConstant());
		// Unbind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		// Update the state
		super.create();
		// Check for errors
		RenderUtil.checkForOpenGLError();
	}

	/**
	 * Uploads the texture to the graphics card. This method has been separated from the create method
	 * for GL30 integrated mipmap support.
	 *
	 * @param buffer The buffer containing the image data
	 * @param width The width of the image
	 * @param height The height of the image
	 */
	protected void uploadTexture(ByteBuffer buffer, int width, int height) {
		if (minFilter.needsMipMaps() && buffer != null) {
			// Build mipmaps if using mip mapped filters
			GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, format.getGLConstant(), width, height, format.getGLConstant(), GL11.GL_UNSIGNED_BYTE, buffer);
		} else {
			// Else just make it a normal texture
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format.getGLConstant(), width, height, 0, format.getGLConstant(), GL11.GL_UNSIGNED_BYTE, buffer);
		}
	}

	@Override
	public void destroy() {
		checkCreated();
		// Activate the unit
		GL13.glActiveTexture(unit);
		// Unbind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		// Delete the texture
		GL11.glDeleteTextures(id);
		// Reset the data
		super.destroy();
		// Check for errors
		RenderUtil.checkForOpenGLError();
	}

	@Override
	public void bind() {
		checkCreated();
		// Activate the unit
		GL13.glActiveTexture(unit);
		// Bind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		// Check for errors
		RenderUtil.checkForOpenGLError();
	}

	@Override
	public void unbind() {
		checkCreated();
		// Activate the unit
		GL13.glActiveTexture(unit);
		// Unbind the texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		// Check for errors
		RenderUtil.checkForOpenGLError();
	}

	@Override
	public GLVersion getGLVersion() {
		return GLVersion.GL20;
	}
}
