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

import org.spout.renderer.Material;
import org.spout.renderer.Model;

public class OpenGL20Model extends Model {
	private final OpenGL20VertexArray vertexArray = new OpenGL20VertexArray();
	private OpenGL20Material material;

	@Override
	public void create() {
		if (created) {
			throw new IllegalStateException("Model has already been created");
		}
		if (material == null) {
			throw new IllegalStateException("Material has not been set");
		}
		vertexArray.setVertexData(vertices);
		vertexArray.create();
		super.create();
	}

	@Override
	public void destroy() {
		checkCreated();
		vertexArray.destroy();
		super.destroy();
	}

	@Override
	protected void render() {
		if (!material.isCreated()) {
			throw new IllegalStateException("Material has not been created yet");
		}
		uniforms.getMatrix4("modelMatrix").set(getMatrix());
		material.getProgram().upload(uniforms);
		vertexArray.render(mode);
	}

	@Override
	public OpenGL20Material getMaterial() {
		return material;
	}

	@Override
	public void setMaterial(Material material) {
		if (!(material instanceof OpenGL20Material)) {
			throw new IllegalArgumentException("Version mismatch: expected OpenGL20Material, got "
					+ material.getClass().getSimpleName());
		}
		this.material = (OpenGL20Material) material;
	}
}