package Configuration;

import java.io.File;
import java.util.Vector;

public class JAR_Configuration {
	
	protected Vector<String> name;
	protected Vector<String> contend;
	
	public final String FILE_NAME;
	public final File FILE;
	public final String PATH_IN_JAR;
	
	public final String TRENNZEICHEN;
	
	public JAR_Configuration(String fileName, String trennzeichen, String pathInJAR) {
		FILE_NAME = fileName;
		TRENNZEICHEN = trennzeichen;
		PATH_IN_JAR = pathInJAR;
		
		FILE = ReadWriteFile.getFileInParentPath(FILE_NAME);
		
		readConfigFromFiles();
		save();
	}
	
	protected void readConfigFromFiles() {
		name = new Vector<String>();
		contend = new Vector<String>();
		
		Vector<String> lines = ReadWriteFile.readFileInJAR(FILE_NAME, PATH_IN_JAR);
		
		for (String line: lines) {
			if (line.contains(TRENNZEICHEN)) {
				int i = line.indexOf(TRENNZEICHEN);
				int i2 = i + TRENNZEICHEN.length();
				name.add(line.substring(0, i));
				contend.add(line.substring(i2));
			}
		}
		
		lines = ReadWriteFile.readFile(FILE);
		
		for (String line: lines) {
			if (line.contains(TRENNZEICHEN)) {
				int i = line.indexOf(TRENNZEICHEN);
				int i2 = i + TRENNZEICHEN.length();
				change(line.substring(0, i), line.substring(i2));
			}
		}
	}
	
	public void save() {
		Vector<String> lines = ReadWriteFile.readFile(FILE);
		Vector<String> file = new Vector<String>();
		Vector<String> names = new Vector<String>();
		for (String line: lines) {
			if (!line.contains(TRENNZEICHEN) )
				file.add(line);
			else {
				line = line.split(TRENNZEICHEN)[0];
				file.add(line + TRENNZEICHEN + get(line));
				names.add(line);
			}
		}
		
		lines = ReadWriteFile.readFileInJAR(FILE_NAME, PATH_IN_JAR);
		Vector<String> addFromOld = new Vector<String>();
		for (String line: lines) {
			if (line.contains(TRENNZEICHEN)) {
				line = line.split(TRENNZEICHEN)[0];
				if (!names.contains(line)) {
					addFromOld.add(line + TRENNZEICHEN + get(line));
					names.add(line);
				}
			}
			else if (file.isEmpty()) {
				addFromOld.add(line);
			}
		}
		
		if(!addFromOld.isEmpty()) {
			file.add("");
			for (String s:addFromOld)
				file.add(s);
		}
		
		Vector<String> newCon = new Vector<String>();
		for (String s: this.name) {
			if (!names.contains(s)) {
				newCon.add(s + TRENNZEICHEN + get(s));
				names.add(s);
			}
		}
		
		if(!newCon.isEmpty()) {
			file.add("");
			for (String s: newCon)
				file.add(s);
		}
		
		try {
			ReadWriteFile.writeFile(FILE, file);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void override(String[] lines) {
		try {
			ReadWriteFile.writeFile(FILE, lines);
		} 
		catch (Exception e) {}
		
		readConfigFromFiles();
	}
	
	public String[] getAllConfig() {
		String[] all = new String[contend.size()];
		for (int i = 0; i < contend.size(); i++) {
			all[i] = name.get(i) + TRENNZEICHEN + contend.get(i);
		}
		return all;
	}
	
	public void add(String name, String value) {
		this.name.add(name);
		contend.add(value);
	}
	
	public void change(String com, String value) {
		if (value.equals(""))
			return;
		if (! name.contains(com))
			add(com, value);
		int i = name.indexOf(com);
		contend.set(i, value);
	}
	
	public void printAll() {
		System.out.println("All:");
		for (int i = 0; i < name.size(); i++) {
			System.out.println(name.get(i)+"  "+TRENNZEICHEN+"  "+contend.get(i));
		}
	}
	
	public int getInt(String name) {
		if (! name.contains(name))
			return 0;
		int i = this.name.indexOf(name);
		try {
			return Integer.parseInt(contend.get(i));
		}
		catch(Exception e) {
			return 0;
		}
	}
	
	public int getInt(String name, int min, int max) {
		int c = getInt(name);
		if (c <= min) {
			return min;
		}
		else if (c >= max) {
			return max;
		}
		return c;
	}
	
	public int getInt(String name, int min, int max, int defaultVal) {
		int c = getInt(name);
		if (c < min) {
			return defaultVal;
		}
		else if (c > max) {
			return defaultVal;
		}
		return c;
	}
	
	public boolean getBool(String name) {
		return getBool(name, false);
	}
	
	public boolean getBool(String name, boolean defaultVal) {
		String c = get(name);
		if ( c.equals("1") || c.equals("true") ) {
			return true;
		}
		else if ( c.equals("0") || c.equals("false") ) {
			return false;
		}
		else {
			return defaultVal;
		}
	}
	
	public String get(String name) {
		if (! this.name.contains(name))
			return "";
		int i = this.name.indexOf(name);
		return contend.get(i);
	}
	
}
