package Configuration;

import java.util.Vector;

public class Configuration implements Cloneable {
	
	protected Vector<String> name;
	protected Vector<String> contend;
	
	public Configuration() {		
		name = new Vector<String>();
		contend = new Vector<String>();
	}
	
	public String[] getAllConfig(String trennzeichen) {
		String[] all = new String[name.size()];
		for (int i = 0; i < name.size(); i++) {
			all[i] = name.get(i) + trennzeichen + contend.get(i);
		}
		return all;
	}
	
	public String[][] getAllConfigAsArrays() {
		String[][] all = new String[2][name.size()];
		all[0] = toArray(name);
		all[1] = toArray(contend);
		return all;
	}
	
	protected String[] toArray(Vector<String> v) {
		String[] s = new String[v.size()];
		for (int i = 0; i < v.size(); i++) {
			s[i] = v.get(i);
		}
		return s;
	}
	
	public void importConfig(String[][] config) {
		for (int i = 0; i < name.size(); i++) {
			change(config[0][i], config[1][i]);
		}
	}
	
	public void importConfig(Configuration con) {
		importConfig(con.getAllConfigAsArrays());
	}
	
	public void add(String name, String value) {
		if (this.name.contains(name))
			change(name, value);
		else {
			this.name.add(name);
			contend.add(value);
		}
	}
	
	public void change(String com, String value) {
		if (! name.contains(com))
			add(com, value);
		else {
			int i = name.indexOf(com);
			contend.set(i, value);
		}
	}
	
	public void printAll() {
		System.out.println("All:");
		for (int i = 0; i < name.size(); i++) {
			System.out.println(name.get(i)+" = "+contend.get(i));
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
		return get(name, "");
	}
	
	public String get(String name, String defaultVal) {
		if (! this.name.contains(name))
			return defaultVal;
		int i = this.name.indexOf(name);
		String s = contend.get(i);
		if (s.equals(""))
			s = defaultVal;
		return s;
	}
	
	public void remove(String name) {
		if (! this.name.contains(name))
			return;
		int i = this.name.indexOf(name);
		this.name.remove(i);
		contend.remove(i);
	}
	
	public Configuration clone() {
		Configuration con = new Configuration();
		for (int i = 0; i < name.size(); i++) {
			con.add(name.get(i), contend.get(i));
		}
		return con;
	}
	
}
