package net.mgsx.pfxr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.pd.Pd;
import net.mgsx.pd.utils.PdAdapter;
import net.mgsx.pfxr.io.PresetParser;
import net.mgsx.pfxr.model.PfxrControl;
import net.mgsx.pfxr.model.Preset;
import net.mgsx.pfxr.model.PresetEntry;
import net.mgsx.pfxr.ui.ControlUI;
import net.mgsx.pfxr.ui.WaveFormUI;
import net.mgsx.pfxr.utils.StageScreen;

public class PFXRScreen extends StageScreen
{
	private Array<ControlUI> sliders = new Array<ControlUI>();

	private boolean disableEvents;
	
	private WaveFormUI finalWaveRenderer;
	private WaveFormUI waveFormRenderer;
	
	private ShapeRenderer shapeRenderer;
	
	private boolean requestUpdate, requestWaveFormUpdate;
	private float updateTimeout;
	
	public PFXRScreen(Array<PfxrControl> controls) 
	{
		Pd.audio.addListener("duration", new PdAdapter(){
			@Override
			public void receiveFloat(String source, float x) {
				updateTimeout = x / 1000f;
				requestUpdate = true;
				requestWaveFormUpdate = true;
			}
		});
		
		finalWaveRenderer = new WaveFormUI(200, 100);
		
		waveFormRenderer = new WaveFormUI(200, 100);
		
		shapeRenderer = new ShapeRenderer();
		
		skin = new Skin(Gdx.files.internal("skins/uiskin.json"));
		
		Table root = new Table(skin);
		root.setFillParent(true);
		stage.addActor(root);
		
		Table controlsTable = new Table(skin);
		
		for(final PfxrControl control : controls){
			
			controlsTable.add(control.name);
			
			ControlUI cui = new ControlUI(control);
			sliders.add(cui);
			
			if(control.labels == null){
				final Slider slider = new Slider(control.min, control.max, (control.max - control.min) / 100f, false, skin);
				cui.slider = slider;
				controlsTable.add(slider);
				slider.addListener(new ChangeListener() {
					@Override
					public void changed(ChangeEvent event, Actor actor) {
						if(!slider.isDragging()){
							Pd.audio.sendFloat(control.name, slider.getValue());
							if(!disableEvents)
								Pd.audio.sendBang("control-change");
						}
					}
				});
			}
			else{
				// select box
				ButtonGroup<Button> group = new ButtonGroup<Button>();
				cui.group = group;
				Table t = new Table();
				for(int i=0 ; i<control.labels.length ; i++){
					String label = control.labels[i];
					final int index = i;
					final TextButton bt = new TextButton(label, skin, "toggle");
					group.add(bt);
					
					t.add(bt).row();
					
					bt.addListener(new ChangeListener() {
						@Override
						public void changed(ChangeEvent event, Actor actor) {
							if(bt.isChecked()){
								Pd.audio.sendFloat(control.name, index);
								if(!disableEvents)
									Pd.audio.sendBang("control-change");
							}
						}
					});
				}
				controlsTable.add(t);
				
			}
			
			final TextButton btLock = new TextButton("lock", skin, "toggle");
			controlsTable.add(btLock);
			controlsTable.row();
			
			btLock.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					control.locked = btLock.isChecked();
				}
			});
			cui.btLock = btLock;
		}
		
		TextButton btTest = new TextButton("Play", skin);
		root.add(btTest).row();
		btTest.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Pd.audio.sendBang("control-change");
			}
		});
		
		TextButton btRand = new TextButton("Randomize", skin);
		root.add(btRand).row();
		btRand.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				randomize();
			}
		});
		
		TextButton btLockAll = new TextButton("Lock All", skin);
		root.add(btLockAll).row();
		btLockAll.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				for(ControlUI c : sliders){
					c.btLock.setChecked(true);
				}
			}
		});
		TextButton btUnLockAll = new TextButton("Unlock All", skin);
		root.add(btUnLockAll).row();
		btUnLockAll.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				for(ControlUI c : sliders){
					c.btLock.setChecked(false);
				}
			}
		});
		
		TextButton btSavePreset = new TextButton("Save Preset", skin);
		root.add(btSavePreset).row();
		btSavePreset.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				savePreset();
			}
		});
		
		TextButton btLoadPreset = new TextButton("Load Preset", skin);
		root.add(btLoadPreset).row();
		btLoadPreset.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				loadPreset();
			}
		});
		
		TextButton btExportAudio = new TextButton("Export Audio", skin);
		root.add(btExportAudio).row();
		btExportAudio.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				exportAudio();
			}
		});
		
		TextButton btUpdateForm = new TextButton("Update form", skin);
		root.add(btUpdateForm).row();
		btUpdateForm.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int size = Pd.audio.arraySize("wave");
				float[] array = new float[size];
				Pd.audio.readArray(array, 0, "wave", 0, size);
				finalWaveRenderer.update(array);
			}
		});
		
		root.add(controlsTable);
	}
	
	private void savePreset() {
		// TODO user define
		Preset preset = new Preset();
		for(ControlUI c : sliders){
			PresetEntry p = new PresetEntry();
			p.name = c.control.name;
			if(c.slider != null)
				p.value = c.slider.getValue();
			else if(c.group != null)
				p.value = c.group.getCheckedIndex();
			preset.entries.add(p);
		}
		new PresetParser().save(preset, Gdx.files.local("../test.txt"));
	}

	protected void loadPreset() {
		// TODO user define
		Preset preset = new PresetParser().parse(Gdx.files.local("../presets/bonus.txt"));
		ObjectMap<String, PresetEntry> map = new ObjectMap<String, PresetEntry>();
		for(PresetEntry p : preset.entries){
			map.put(p.name, p);
		}
		
		disableEvents = true;
		for(ControlUI c : sliders){
			PresetEntry p = map.get(c.control.name);
			if(p != null){
				if(c.slider != null)
					c.slider.setValue(p.value);
				else if(c.group != null){
					int index = MathUtils.round(p.value);
					if(index >= 0 && index < c.group.getButtons().size){
						c.group.getButtons().get(index).setChecked(true);
					}
				}
			}
		}
		disableEvents = false;
		Pd.audio.sendBang("control-change");
	}

	private void exportAudio() {
		// TODO user enter a path ...
		Pd.audio.sendSymbol("export-wave", "../test.wav");
	}

	private void randomize() {
		disableEvents = true;
		for(ControlUI c : sliders){
			if(!c.control.locked){
				if(c.slider != null)
					c.slider.setValue(MathUtils.random(c.control.min, c.control.max));
				else if(c.group != null){
					int index = MathUtils.random(c.control.labels.length-1);
					if(index >= 0 && index < c.group.getButtons().size){
						c.group.getButtons().get(index).setChecked(true);
					}
				}
			}
		}
		disableEvents = false;
		Pd.audio.sendBang("control-change");
	}

	@Override
	public void render(float delta) {
		
		if(requestWaveFormUpdate){
			requestWaveFormUpdate = false;
			int size = Pd.audio.arraySize("waveform");
			float[] array = new float[size];
			Pd.audio.readArray(array, 0, "waveform", 0, size);
			waveFormRenderer.update(array);
			System.out.println("update WaveForm");
		}
		
		if(requestUpdate){
			updateTimeout -= delta;
			if(updateTimeout < 0){
				requestUpdate = false;
				int size = Pd.audio.arraySize("wave");
				float[] array = new float[size];
				Pd.audio.readArray(array, 0, "wave", 0, size);
				finalWaveRenderer.update(array);
				System.out.println("update");
			}
		}
		
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render(delta);
		
		shapeRenderer.getTransformMatrix().idt();
		shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shapeRenderer.updateMatrices();
		finalWaveRenderer.draw(shapeRenderer);
		
		shapeRenderer.getTransformMatrix().setToTranslation(0, 200, 0);
		shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shapeRenderer.updateMatrices();
		waveFormRenderer.draw(shapeRenderer);
	}
}
