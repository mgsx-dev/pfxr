package net.mgsx.pfxr.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class WaveFormUI {
	private float [] vertices;
	private int width, height;
	private FrameBuffer fbo;
	private ShapeRenderer renderer;
	private Color bgColor, fgColor;
	
	public WaveFormUI(int width, int height, Color color) {
		super();
		bgColor = new Color(color).mul(.3f);
		fgColor = new Color(color);
		this.width = width;
		this.height = height;
		vertices = new float[width * 2];
		renderer = new ShapeRenderer();
		renderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
	}

	public void update(float [] data){
		for(int x=0 ; x<width ; x++){
			float t = (float)x / (float)width;
			int index = (int)(t * data.length);
			float y = ((data[index] * 0.9f + 1) / 2);
			vertices[x * 2 + 0] = x;
			vertices[x * 2 + 1] = y * height;
		}
		draw();
	}
	
	private void draw(){
		fbo.begin();
		Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		renderer.begin(ShapeType.Line);
		renderer.setColor(fgColor);
		renderer.polyline(vertices, 0, vertices.length);
		renderer.end();
		fbo.end();
	}

	public Texture getTexture() {
		return fbo.getColorBufferTexture();
	}
}
