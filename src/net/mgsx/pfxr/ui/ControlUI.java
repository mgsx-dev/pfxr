package net.mgsx.pfxr.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import net.mgsx.pfxr.model.PfxrControl;

public class ControlUI extends Table
{
	public PfxrControl control;
	public Slider slider;
	public TextButton btLock;
	public ButtonGroup<Button> group;

	public ControlUI(PfxrControl control) {
		super();
		this.control = control;
	}
	
	
}
