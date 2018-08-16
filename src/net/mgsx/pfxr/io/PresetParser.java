package net.mgsx.pfxr.io;

import java.util.Scanner;

import com.badlogic.gdx.files.FileHandle;

import net.mgsx.pfxr.model.Preset;
import net.mgsx.pfxr.model.PresetEntry;

public class PresetParser {

	public Preset parse(FileHandle file){
		Preset preset = new Preset();
		
		final Scanner s = new Scanner(file.read());
		while(s.hasNextLine()) {
		    final String line = s.nextLine();
		    preset.entries.add(parse(line));
		}
		s.close();
		return preset;
	}

	private PresetEntry parse(String line) {
		String [] separated = line.split(";");
		String [] keyValue = separated[0].split(" ");
		PresetEntry e = new PresetEntry();
		e.name = keyValue[0];
		e.value = Float.parseFloat(keyValue[1]);
		return e;
	}

	public void save(Preset preset, FileHandle file) {
		String s = "";
		for(PresetEntry p : preset.entries){
			s += p.name + " " + p.value + ";\n";
		}
		file.writeString(s, false);
	}
}
