package net.mgsx.pfxr;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

public class PFXR extends ApplicationAdapter 
{
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		// TODO configure your launcher and remove the this line
		// begin configuration
		config.width = 640;
		config.height = 480;
		// end configuration
		new LwjglApplication(new PFXR(), config);
	}

	public PFXR() {
		super();
		// TODO contruct
	}
	
	@Override
	public void create () {
		// TODO init sketch
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// TODO render sketch
	}
	
	@Override
	public void dispose () {
		// TODO dispose sketch
	}
}
