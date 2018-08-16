package net.mgsx.pfxr;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import net.mgsx.pd.Pd;
import net.mgsx.pd.PdConfiguration;
import net.mgsx.pfxr.io.PdParser;
import net.mgsx.pfxr.io.PdParser.PdCanvas;
import net.mgsx.pfxr.io.PdParser.PdObject;
import net.mgsx.pfxr.model.PfxrControl;

public class PFXR extends Game 
{
	private static final String pdPath = "pd";
	
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 750;
		config.height = 900;
		new LwjglApplication(new PFXR(), config);
	}

	@Override
	public void create () {
		PdConfiguration config = new PdConfiguration();
		Pd.audio.create(config);
		
		FileHandle patch = Gdx.files.local(pdPath + "/pfxr.pd");
		
		PdCanvas patchData = PdParser.parsePatch(patch);
		
		Array<PfxrControl> controls = new Array<PfxrControl>();
		for(PdObject object : patchData.objects){
			if("control".equals(object.name)){
				PfxrControl c = new PfxrControl();
				c.name = object.arguments[0];
				c.min = Float.parseFloat(object.arguments[1]);
				c.max = Float.parseFloat(object.arguments[2]);
				controls.add(c);
			}
			else if("control-select".equals(object.name)){
				PfxrControl c = new PfxrControl();
				c.name = object.arguments[0];
				int count = Integer.parseInt(object.arguments[1]);
				c.min = 0;
				c.max = count-1;
				c.steps = 1;
				if("type".equals(c.name)){
					c.labels = new String[]{"Triangle", "Sin", "Square", "Saw", "White"};
				}
				controls.add(c);
			}
		}
		
		Pd.audio.open(patch);
		
		setScreen(new PFXRScreen(controls));
	}

	@Override
	public void dispose () {
		Pd.audio.dispose();
	}
}
