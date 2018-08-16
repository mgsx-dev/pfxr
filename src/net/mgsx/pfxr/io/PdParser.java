package net.mgsx.pfxr.io;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class PdParser {

	public static class PdObject {

		public float x, y;
		public String name;
		public String [] arguments;

	}
	public static class PdWidget {

		public float x, y, width, height;

	}
	public static class PdVerticalSlider extends PdWidget
	{
		public float min, max;
		public String label, send;
	}
	
	public static class PdArray 
	{
		public String name, type;
		public int length, drawType;
		public boolean save;
		public float [] buffer;
	}
	public static class PdCanvas extends PdWidget{
		public float left, right, top, bottom;
		
		public List<PdWidget> widgets = new ArrayList<PdWidget>();
		
		public PdArray array;
		public Array<PdObject> objects = new Array<PdObject>();
		
		public void add(PdWidget widget) {
			widgets.add(widget);
		}
		
	}
	public static PdCanvas parsePatch(FileHandle patch)
	{
		try {
			return new PdParser().parse(patch);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private LinkedList<String> atom = new LinkedList<String>();
	private LinkedList<PdCanvas> patchStack = new LinkedList<PdCanvas>();
	
	private PdCanvas parse(FileHandle patch) throws FileNotFoundException
	{
		final Scanner s = new Scanner(patch.read());
		while(s.hasNextLine()) {
		    final String line = s.nextLine();
		    parse(line);
		}
		s.close();
		return patchStack.removeFirst();
	}

	private void parse(String line) 
	{
		parse(line.split(" "));
	}

	private void parse(String[] tokens) 
	{
		atom.addAll(Arrays.asList(tokens));
		if(tokens[tokens.length-1].endsWith(";"))
		{
			String last = atom.removeLast();
			atom.addLast(last.substring(0, last.length()-1));
			parseAtom();
			atom.clear();
		}
	}

	private void parseAtom() 
	{
		String token = atom.removeFirst();
		if("#N".equals(token))
		{
			PdCanvas canvas = new PdCanvas();
			patchStack.addLast(canvas);
		}
		
		PdCanvas canvas = patchStack.peekLast();

		String type = atom.removeFirst();
		if("restore".equals(type))
		{
			canvas.x = Float.parseFloat(atom.removeFirst());
			canvas.y = Float.parseFloat(atom.removeFirst());
			PdCanvas subPatch = patchStack.removeLast();
			canvas = patchStack.peekLast();
			canvas.add(subPatch);
		}
		
		if("canvas".equals(type))
		{
			atom.removeFirst();
			atom.removeFirst();
			canvas.width = Float.parseFloat(atom.removeFirst());
			canvas.height = Float.parseFloat(atom.removeFirst());
		}
		else if("array".equals(type))
		{
			PdArray array = new PdArray();
			canvas.array = array;
			
			array.name = atom.removeFirst();
			array.length = Integer.parseInt(atom.removeFirst());
			array.type = atom.removeFirst();
			int options = Integer.parseInt(atom.removeFirst());
			array.save = (options & 1) != 0;
			array.drawType = options >> 1;
			array.buffer = new float[array.length];
		}
		else if("coords".equals(type))
		{
			canvas.left = Float.parseFloat(atom.removeFirst());
			canvas.top = Float.parseFloat(atom.removeFirst());
			canvas.right = Float.parseFloat(atom.removeFirst());
			canvas.bottom = Float.parseFloat(atom.removeFirst());
			canvas.width = Float.parseFloat(atom.removeFirst());
			canvas.height = Float.parseFloat(atom.removeFirst());
		}
		else if("obj".equals(type))
		{
			float x = Float.parseFloat(atom.removeFirst());
			float y = Float.parseFloat(atom.removeFirst());
			
			String subType = atom.removeFirst();
			
			if("vsl".equals(subType))
			{
				PdVerticalSlider vsl = new PdVerticalSlider();
				vsl.x = x;
				vsl.y = y;
				vsl.width = Float.parseFloat(atom.removeFirst());
				vsl.height = Float.parseFloat(atom.removeFirst());
				vsl.min = Float.parseFloat(atom.removeFirst());
				vsl.max = Float.parseFloat(atom.removeFirst());
				atom.removeFirst();
				atom.removeFirst();
				vsl.send = atom.removeFirst();
				atom.removeFirst();
				vsl.label = atom.removeFirst();
				atom.removeFirst();
				canvas.add(vsl);
			}
			// Non GUI objects : just store them
			else{
				PdObject object = new PdObject();
				object.x = x;
				object.y = y;
				object.name = subType;
				object.arguments = new String[atom.size()];
				for(int i=0 ; i<object.arguments.length ; i++){
					object.arguments[i] = atom.removeFirst();
				}
				canvas.objects.add(object);
			}
		}
	}
}
