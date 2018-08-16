package net.mgsx.pfxr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class WaveFormUI {
	private float [] vertices;
	private int width, height;
	
	public WaveFormUI(int width, int height) {
		super();
		this.width = width;
		this.height = height;
		vertices = new float[width * 2];
	}

	public void update(float [] data){
		for(int x=0 ; x<width ; x++){
			float t = (float)x / (float)width;
			int index = (int)(t * data.length);
			float y = (data[index] + 1) / 2;
			vertices[x * 2 + 0] = x;
			vertices[x * 2 + 1] = y * height;
		}
	}
	
	public void draw(ShapeRenderer renderer){
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.BLUE);
		renderer.polyline(vertices, 0, vertices.length/2);
		renderer.end();
	}
}
