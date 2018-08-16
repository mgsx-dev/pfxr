package net.mgsx.pfxr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
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
	private static final String presetPath = "presets";
	private static final String audioPath = "audio";
	
	private Array<ControlUI> sliders = new Array<ControlUI>();
	
	private Color bg = new Color(Color.WHITE).mul(.2f);

	private boolean disableEvents;
	
	private WaveFormUI finalWaveRenderer;
	private WaveFormUI waveFormRenderer;
	
	private boolean requestUpdate, requestWaveFormUpdate;
	private float updateTimeout;
	
	private String lastPresetName = "";
	
	private ObjectMap<String, PfxrControl> controlMap = new ObjectMap<String, PfxrControl>();
	private Table controlsTable;
	
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
		
		finalWaveRenderer = new WaveFormUI(200, 100, Color.ORANGE);
		
		waveFormRenderer = new WaveFormUI(200, 100, Color.YELLOW);
		
		skin = new Skin(Gdx.files.internal("assets/skins/uiskin.json"));
		
		Table root = new Table(skin);
		root.setFillParent(true);
		stage.addActor(root);
		
		controlsTable = new Table(skin);
		controlsTable.defaults().pad(4);
		
		
		for(final PfxrControl control : controls){
			
			controlMap.put(control.name, control);
			
		}
		
		appendGroup("Envelop");
		
		appendControl("attack-time");
		appendControl("sustain-time");
		appendControl("decay-time");
		
		appendGroup("Wave Form");
		
		appendControl("type");
		
		appendGroup("Pitch");
		
		appendControl("frequency");
		
		appendControl("pitch-slide");
		
		appendControl("vibrato-speed");
		appendControl("vibrato-depth");
		
		appendGroup("Tremolo");
		
		appendControl("tremolo-depth");
		appendControl("tremolo-speed");
		
		appendGroup("Bit Crusher");
		
		appendControl("bit-crush");
		appendControl("bit-sweep");
		
		appendGroup("Pitch Jumper");
		
		appendControl("pitch-jump-speed");
		appendControl("pitch-jump-range");
		
		
			
		Table menu = new Table(skin);
		menu.defaults().pad(10).fill();
		
		Label lbTitle = menu.add("PFXR").getActor();
		lbTitle.setFontScale(2);
		lbTitle.setColor(Color.ROYAL);
		menu.row();
		
		
		menu.add("Playground").padBottom(2).getActor().setColor(Color.LIGHT_GRAY);
		menu.row();
		
		TextButton btTest = new TextButton("Play", skin);
		menu.add(btTest).row();
		btTest.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Pd.audio.sendBang("control-change");
			}
		});
		
		TextButton btRand = new TextButton("Randomize", skin);
		menu.add(btRand).row();
		btRand.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				randomize();
			}
		});
		
		TextButton btLockAll = new TextButton("Lock All", skin);
		menu.add(btLockAll).row();
		btLockAll.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				for(ControlUI c : sliders){
					c.btLock.setChecked(true);
				}
			}
		});
		TextButton btUnLockAll = new TextButton("Unlock All", skin);
		menu.add(btUnLockAll).row();
		btUnLockAll.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				for(ControlUI c : sliders){
					c.btLock.setChecked(false);
				}
			}
		});
		
		menu.add("File management").padBottom(2).getActor().setColor(Color.LIGHT_GRAY);
		menu.row();
		
		TextButton btSavePreset = new TextButton("Save Preset", skin);
		menu.add(btSavePreset).row();
		btSavePreset.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				savePreset();
			}
		});
		
		TextButton btLoadPreset = new TextButton("Load Preset", skin);
		menu.add(btLoadPreset).row();
		btLoadPreset.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				loadPreset();
			}
		});
		
		TextButton btExportAudio = new TextButton("Export Audio", skin);
		menu.add(btExportAudio).row();
		btExportAudio.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				exportAudio();
			}
		});
		
		menu.add("Wave form preview").padBottom(2).getActor().setColor(Color.YELLOW);
		menu.row();
		menu.add(new Image(waveFormRenderer.getTexture())).row();
		
		menu.add("Final SFX preview").padBottom(2).getActor().setColor(Color.ORANGE);
		menu.row();
		menu.add(new Image(finalWaveRenderer.getTexture())).row();
		
		root.add(menu);
		root.add(controlsTable);
		
		loadPreset(Gdx.files.local(presetPath + "/bonus.txt"));
		
		stage.addAction(Actions.sequence(Actions.delay(1), Actions.run(new Runnable() {
			@Override
			public void run() {
				requestWaveFormUpdate = true;
			}
		})));
		
	}
	
	private void appendGroup(String name) {
		controlsTable.add(name).colspan(2).expandX().left().getActor().setColor(Color.ROYAL);;
		controlsTable.row();
	}

	private void appendControl(String name) {
		appendControl(controlsTable, controlMap.get(name));
	}

	private void appendControl(Table controlsTable, final PfxrControl control)
	{
		controlsTable.add(control.name);
		
		
		ControlUI cui = new ControlUI(control);
		sliders.add(cui);
		
		if(control.labels == null){
			final Label valueLabel = new Label(String.valueOf(control.min), skin);
			valueLabel.setColor(Color.GRAY);
			
			final Slider slider = new Slider(control.min, control.max, (control.max - control.min) / 100f, false, skin);
			cui.slider = slider;
			controlsTable.add(slider);
			controlsTable.add(valueLabel).width(80);
			slider.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if(!slider.isDragging()){
						Pd.audio.sendFloat(control.name, slider.getValue());
						if(!disableEvents)
							Pd.audio.sendBang("control-change");
					}
					valueLabel.setText(String.valueOf(slider.getValue()));
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
				
				t.add(bt).fill().row();
				
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
			controlsTable.add();
			
		}
		
		// force to workaround no slider update if same value
		Pd.audio.sendFloat(control.name, control.min);
		
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
	
	private void savePreset(FileHandle file) {
		
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
		new PresetParser().save(preset, file);
	}

	protected void loadPreset() {
		
		final Dialog dialog = new Dialog("Load Preset", skin);
		dialog.getStyle().stageBackground = skin.newDrawable("white", new Color(0, 0, 0, .75f));
		dialog.setMovable(false);
		Table presetList = new Table(skin);
		presetList.defaults().fill().padRight(20).padLeft(20);
		
		for(final FileHandle file : Gdx.files.local(presetPath).list("txt")){
			TextButton btPreset = new TextButton(file.nameWithoutExtension(), skin);
			presetList.add(btPreset).row();
			btPreset.addListener(new ChangeListener() {
				
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					loadPreset(file);
					dialog.hide();
				}
			});
		}
		
		ScrollPane sp = new ScrollPane(presetList, skin);
		sp.setHeight(100);
		sp.setScrollingDisabled(true, false);
		sp.setForceScroll(false, true);
		
		Table t = new Table();
		t.add(sp).growX().height(300);
		
		dialog.getContentTable().add(t);
		
		dialog.button("Cancel", null);
		
		dialog.show(stage);
	}
	
	protected void savePreset() {
		
		final TextField tf = new TextField(lastPresetName, skin);
		
		final Dialog dialog = new Dialog("Save Preset", skin){
			@Override
			protected void result(Object object) {
				if(Boolean.TRUE.equals(object)){
					String name = tf.getText().trim();
					if(!name.isEmpty()){
						lastPresetName = name;
						savePreset(Gdx.files.local(presetPath).child(name + ".txt"));
					}
				}
			}
		};
		dialog.getStyle().stageBackground = skin.newDrawable("white", new Color(0, 0, 0, .75f));
		dialog.setMovable(false);
		
		final Label lb = new Label("File already exists and will be replaced", skin);
		lb.setColor(Color.ORANGE);
		lb.setVisible(false);
		
		dialog.getContentTable().defaults().fill();
		dialog.getContentTable().add(tf).row();
		dialog.getContentTable().add(lb).row();
		
		dialog.button("OK", true);
		dialog.button("Cancel", false);
		
		dialog.show(stage);
		
		tf.setTextFieldListener(new TextFieldListener() {
			@Override
			public void keyTyped(TextField textField, char c) {
				if(Gdx.files.local(presetPath).child(textField.getText().trim() + ".txt").exists()){
					lb.setVisible(true);
				}else{
					lb.setVisible(false);
				}
			}
		});
	}
	
	protected void exportAudio() {
		
		final TextField tf = new TextField(lastPresetName, skin);
		
		final Dialog dialog = new Dialog("Export Audio", skin){
			@Override
			protected void result(Object object) {
				if(Boolean.TRUE.equals(object)){
					String name = tf.getText().trim();
					if(!name.isEmpty()){
						lastPresetName = name;
						exportAudio(Gdx.files.local(audioPath).child(name + ".wav"));
					}
				}
			}
		};
		dialog.getStyle().stageBackground = skin.newDrawable("white", new Color(0, 0, 0, .75f));
		dialog.setMovable(false);
		
		final Label lb = new Label("File already exists and will be replaced", skin);
		lb.setColor(Color.ORANGE);
		lb.setVisible(false);
		
		dialog.getContentTable().defaults().fill();
		dialog.getContentTable().add(tf).row();
		dialog.getContentTable().add(lb).row();
		
		dialog.button("OK", true);
		dialog.button("Cancel", false);
		
		dialog.show(stage);
		
		tf.setTextFieldListener(new TextFieldListener() {
			@Override
			public void keyTyped(TextField textField, char c) {
				if(Gdx.files.local(audioPath).child(textField.getText().trim() + ".wav").exists()){
					lb.setVisible(true);
				}else{
					lb.setVisible(false);
				}
			}
		});
	}

	protected void loadPreset(FileHandle file) {
		Preset preset = new PresetParser().parse(file);
		ObjectMap<String, PresetEntry> map = new ObjectMap<String, PresetEntry>();
		for(PresetEntry p : preset.entries){
			map.put(p.name, p);
		}
		
		lastPresetName = file.nameWithoutExtension();
		
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

	private void exportAudio(FileHandle file) {
		Pd.audio.sendSymbol("export-wave", file.file().getAbsolutePath());
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
		}
		
		if(requestUpdate){
			updateTimeout -= delta;
			if(updateTimeout < 0){
				requestUpdate = false;
				int size = Pd.audio.arraySize("wave");
				float[] array = new float[size];
				Pd.audio.readArray(array, 0, "wave", 0, size);
				finalWaveRenderer.update(array);
			}
		}
		
		Gdx.gl.glClearColor(bg.r, bg.g, bg.b, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render(delta);
	}
}
