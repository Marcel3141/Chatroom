
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Calendar;
import java.util.Vector;

import Configuration.ReadWriteFile;
import Configuration.Configuration;

public class Updater implements Runnable {
	
	protected int id;
	protected Vector<Integer> errorIds;
	protected int lastId;
	protected boolean online;
	protected Calendar nextCheckTime;
	
	protected StartClass serverClass;
	protected StartClass newServerClass;
	protected Configuration serverCon;
	
	public Updater(String pw) {
		id = -1;
		lastId = -1;
		online = false;
		serverClass = null;
		newServerClass = null;
		nextCheckTime = Calendar.getInstance();
		errorIds = new Vector<Integer>();
		
		int f = tryGetUpdate();
		while (f == -1) {
			pl("Error -1 pos 1");
			try {
				Thread.sleep(10000);//3600000);
			}
			catch(Exception e) {}
			f = tryGetUpdate();
		}
		pl("downloaded");
		calculateNextCheckTime();
		
		serverClass = newServerClass;
		newServerClass = null;
		serverClass.start(pw);
		
		serverCon = serverClass.getConfig();
		
		new Thread(this).start();
	}
	
	public void run() {
		while (true) {
			int time = Calendar.getInstance().compareTo(nextCheckTime);
			if (time <= 0) {
				int retVal = tryGetUpdate();
				if (retVal == 0) {
					startNewServer();
					calculateNextCheckTime(15);//();
				}
				else if (retVal == -1) {
					pl("Error -1");
					calculateNextCheckTime(5);
					continue;
				}
			}
			else {
				try {
					Thread.sleep(time + 1);
				}
				catch(Exception e) {}
				continue;
			}
		}
	}
	
	public void startNewServer() {
		serverCon = serverClass.getConfig();
		serverClass.pause();
		newServerClass.start(serverCon.clone());
		serverClass.stop();
		serverClass = newServerClass;
		newServerClass = null;
	}
	
	public void calculateNextCheckTime(int minutes) {
		nextCheckTime = Calendar.getInstance();
		nextCheckTime.add(Calendar.MINUTE, minutes);
	}
	
	public void calculateNextCheckTime() {
		nextCheckTime = Calendar.getInstance();
		if ( nextCheckTime.get(Calendar.HOUR_OF_DAY) > 3)
			nextCheckTime.add(Calendar.DAY_OF_YEAR, 1);
		nextCheckTime.set(Calendar.HOUR_OF_DAY, 4);
		nextCheckTime.set(Calendar.MINUTE, 0);
	}
	
	public static void main(String[] args) {
		new Updater("1");
	}
	
	public int tryGetUpdate() {
		while (true) {
			id = getId();
			int i = 0;
			while(id == -1) {
				try {
					if (i < 6)
						Thread.sleep(10000);
					else if (i < 65)
						Thread.sleep(60000);
					else
						Thread.sleep(600000);
				}
				catch(Exception e) {}
				i++;
				id = getId();
			}
			if ( id <= lastId ) {
				return -2;
			}
			if ( errorIds.contains(id) ) {
				return -1;
			}
			i = 0;
			while ( id > lastId ) {
				if ( errorIds.contains(id) ) {
					return -1;
				}
				int val = downloadFile();
				if (val == 0) {
					lastId = id;
					return 0;
				}
				else if(val == -1) {
					//errorIds.add(id);
					return -1;
				}
				else {
					try {
						if (i < 6)
							Thread.sleep(10000);
						else if (i < 65)
							Thread.sleep(60000);
						else
							Thread.sleep(600000);
					}
					catch(Exception e) {}
					i++;
					id = getId();
				}
			}
		}
	}
	
	public int getId() {
		String s = "";
		try {
			final URL url = new URL("http://dl.dropbox.com/u/89839665/Chatroom/id.txt");
			final URLConnection conn = url.openConnection();
			final InputStream is = new BufferedInputStream(conn.getInputStream());
			byte[] chunk = new byte[1024];
			int chunkSize;
			while ((chunkSize = is.read(chunk)) != -1) {
				for (int i = 0; i < chunkSize; i++) {
					s += ((char) chunk[i]);
				}
			}
			is.close();
		}
		catch(Exception e) {
			return -1;
		}
		int id = -1;
		try {
			id = Integer.parseInt(s);
		}
		catch(Exception e) {}
		return id;
	}
	
	public int downloadFile() {
		Class<? extends StartClass> strtClss;
		try {
			final String name = "Server";
			final URL url = new URL("http://dl.dropbox.com/u/89839665/Chatroom/"+ name +".jar");
			final URLConnection conn = url.openConnection();
			final InputStream is = new BufferedInputStream(conn.getInputStream());
			final OutputStream os = new BufferedOutputStream(new FileOutputStream(name +".jar"));
			byte[] chunk = new byte[1024];
			int chunkSize;
			while ((chunkSize = is.read(chunk)) != -1) {
				os.write(chunk, 0, chunkSize);
			}
			os.close();
			is.close();
			File file  = ReadWriteFile.getFileInParentPath(name +".jar");
			URL url1 = file.toURI().toURL();
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { url1 });
			Class<?> clazz = Class.forName(name, true, loader);
			strtClss = clazz.asSubclass(StartClass.class);
		}
		catch(Exception e) {
			return -2;
		}
		catch(Throwable e) {
			pl("XD.XD.XD.XD");
			e.printStackTrace();
			//System.exit(1);
			return -2;
		}
		try {
			newServerClass = strtClss.newInstance();
			return 0;
		}
		catch(Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public void pl(String s) {
		System.out.println(s);
	}
	
}
