package com.kingbob.RocketRider;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.makersf.andengine.extension.collisions.entity.sprite.PixelPerfectSprite;
import com.makersf.andengine.extension.collisions.opengl.texture.region.PixelPerfectTextureRegion;

public class Wall extends PixelPerfectSprite {
	private float xPos;
	private float yPos;
	private PhysicsHandler handler;

	public Wall(float pX, float pY, PixelPerfectTextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		xPos = pX;
		yPos = pY;
	}

	public float getXPos() {
		return xPos;
	}

	public void setXPos(float pX) {
		xPos = pX;
	}

	public void setYPos(float pY) {
		yPos = pY;
	}

	public float getYPos() {
		return yPos;
	}

	public void setHandler(PhysicsHandler pHandler) {
		handler = pHandler;
	}

	public PhysicsHandler getHandler() {
		return handler;
	}
}
