package Configuration;

import java.io.File;
import java.util.Vector;

public class JAR_Configuration extends Configuration implements Cloneable {
	
	public final String FILE_NAME;
	public final File FILE;
	public final String PATH_IN_JAR;
	public final boolean HAS_MASTERFILE;
	
	public final String TRENNZEICHEN;
	
	public JAR_Configuration(String fileName, String trennzeichen) {
		super();
		FILE_NAME = fileName;
		TRENNZEICHEN = trennzeichen;
		PATH_IN_JAR = "";
		HAS_MASTERFILE = false;
		
		FILE = ReadWriteFile.getFileInParentPath(FILE_NAME);
		
		startup();
	}
	
	public JAR_Configuration(String fileName, String trennzeichen, String pathInJAR) {
		super();
		FILE_NAME = fileName;
		TRENNZEICHEN = trennzeichen;
		PATH_IN_JAR = pathInJAR;
		HAS_MASTERFILE = true;
		
		FILE = ReadWriteFile.getFileInParentPath(FILE_NAME);
		
		startup();
	}
	
	public JAR_Configuration(String fileName, String trennzeichen, String pathInJAR, boolean masterfile) {
		super();
		FILE_NAME = fileName;
		TRENNZEICHEN = trennzeichen;
		PATH_IN_JAR = pathInJAR;
		HAS_MASTERFILE = masterfile;
		
		FILE = ReadWriteFile.getFileInParentPath(FILE_NAME);
		
		startup();
	}
	
	public JAR_Configuration(String fileNameInJar, File externalFile, String trennzeichen, String pathInJAR, boolean masterfile) {
		super();
		FILE_NAME = fileNameInJar;
		TRENNZEICHEN = trennzeichen;
		PATH_IN_JAR = pathInJAR;
		HAS_MASTERFILE = masterfile;
		FILE = externalFile;
		
		startup();
	}
	
	protected void startup() {
		readConfig();
		save();
	}
	
	protected void readConfig() {
		Vector<String> name = new Vector<String>();
		Vector<String> contend = new Vector<String>();
		Vector<String> lines;
		
		if (HAS_MASTERFILE) {
			lines = ReadWriteFile.readFileInJAR(FILE_NAME, PATH_IN_JAR);
			
			for (String line: lines) {
				if (line.contains(TRENNZEICHEN)) {
					int i = line.indexOf(TRENNZEICHEN);
					int i2 = i + TRENNZEICHEN.length();
					String com = line.substring(0, i);
					String value = line.substring(i2);
					if (com.equals(""))
						continue;
					name.add(com);
					contend.add(value);
				}
			}
		}
		
		lines = ReadWriteFile.readFile(FILE);
		
		for (String line: lines) {
			if (line.contains(TRENNZEICHEN)) {
				int i = line.indexOf(TRENNZEICHEN);
				int i2 = i + TRENNZEICHEN.length();
				String com = line.substring(0, i);
				String value = line.substring(i2);
				if (HAS_MASTERFILE) {
					if (value.equals(""))
						continue;
					if (! name.contains(com)){
						if (com.equals("")) 
							continue;
						name.add(com);
						contend.add(value);
					}
					else {
						int indx = name.indexOf(com);
						contend.set(indx, value);
					}
				}
				else {
					if (com.equals(""))
						continue;
					name.add(com);
					contend.add(value);
				}
			}
		}
		
		this.name = name;
		this.contend = contend;
		
	}
	
	public void save() {
		Vector<String> lines = ReadWriteFile.readFile(FILE);
		Vector<String> file = new Vector<String>();
		Vector<String> names = new Vector<String>();
		
		for (String line: lines) {
			if (!line.contains(TRENNZEICHEN) )
				file.add(line);
			else {
				String[] param = line.split(TRENNZEICHEN);
				if (! get(param[0]).equals(""))
					file.add(param[0] + TRENNZEICHEN + get(param[0]));
				else
					file.add(line);
				names.add(param[0]);
			}
		}
		
		if ( HAS_MASTERFILE ) {
			lines = ReadWriteFile.readFileInJAR(FILE_NAME, PATH_IN_JAR);
			Vector<String> addFromOld = new Vector<String>();
			for (String line: lines) {
				if (line.contains(TRENNZEICHEN)) {
					String[] param = line.split(TRENNZEICHEN);
					if (!names.contains(param[0])) {
						if (! get(param[0]).equals(""))
							addFromOld.add(param[0] + TRENNZEICHEN + get(param[0]));
						else
							addFromOld.add(line);
						names.add(param[0]);
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
		
		readConfig();
	}
	
	public String[] getAllConfig() {
		return super.getAllConfig(TRENNZEICHEN);
	}
	
	public void printAll() {
		System.out.println("All:");
		for (int i = 0; i < name.size(); i++) {
			System.out.println(name.get(i)+"  "+TRENNZEICHEN+"  "+contend.get(i));
		}
	}
	
	public JAR_Configuration clone() {
		JAR_Configuration con = new JAR_Configuration(FILE_NAME, FILE, TRENNZEICHEN, PATH_IN_JAR, HAS_MASTERFILE);
		for (int i = 0; i < name.size(); i++) {
			con.change(name.get(i), contend.get(i));
		}
		return con;
	}
	
	public Configuration getConfiguration() {
		return super.clone();
	}
	
}
