package Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;
import java.io.IOException;

public class ReadWriteFile {
	
	public static Vector<String> readFileInJAR(String fileName, String pathInJAR) {
		String path = "/" + pathInJAR;
		path += "/" + fileName;
		ReadWriteFile rwf = new ReadWriteFile();		
		Vector<String> lines = new Vector<String>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(rwf.getClass().getResourceAsStream(path)));
			while (reader.ready())
				lines.add(reader.readLine());
			reader.close();
		}
		catch (Exception e) {}
		
		return lines;
	}
	
	public static File getFileInParentPath(String fileName) {
		String path = ReadWriteFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		return new File(new File(path).getParentFile().getPath(), fileName);
	}
	
	public static Vector<String> readFile(File file) {
		Vector<String> lines = new Vector<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while (reader.ready())
					lines.add(reader.readLine());
			reader.close();
		}
		catch (Exception e) {}
		return lines;
	}
	
	public static void writeFile(File file, String[] contend) throws IOException{
		PrintWriter writer = new PrintWriter(file);
		for (String s: contend) {
			writer.println(s);
		}
		writer.close();
	}
	
	public static void writeFile(File file, Vector<String> contend) throws IOException{
		PrintWriter writer = new PrintWriter(file);
		for (String s: contend) {
			writer.println(s);
		}
		writer.close();
	}
	
}
