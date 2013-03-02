
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

/**
 * @author Marcel Kramer
 */
public class Updater implements Runnable {
	
	protected int id;
	protected Vector<Integer> errorIds;
	protected int lastId;
	protected boolean online;
	protected Calendar nextCheckTime;
	
	protected StartClass serverClass;
	protected StartClass newServerClass;
	protected Configuration serverCon;
	
	public Updater(String pw, int port) {
		id = -1;
		lastId = -1;
		online = false;
		serverClass = null;
		newServerClass = null;
		nextCheckTime = Calendar.getInstance();
		errorIds = new Vector<Integer>();
		
		int f = tryGetUpdate();
		while (f == -1) {
			try {
				Thread.sleep(3600000);
			}
			catch(Exception e) {}
			f = tryGetUpdate();
		}
		calculateNextCheckTime();
		
		serverClass = newServerClass;
		newServerClass = null;
		serverClass = serverClass.start(pw, port);
		pl("AT started");
		serverCon = serverClass.getConfig();
		
		new Thread(this).start();
	}
	
	public void run() {
		while (true) {
			Calendar now = Calendar.getInstance();
			int time = (int) (nextCheckTime.getTimeInMillis() - now.getTimeInMillis());
			if (time <= 0) {
				int retVal = tryGetUpdate();
				if (retVal == 0) {
					startNewServer();
					calculateNextCheckTime();
				}
				else if (retVal == -1) {
					calculateNextCheckTime(5);
					continue;
				}
				else {
					pl("no update avalable");
					calculateNextCheckTime();
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
		newServerClass = newServerClass.start(serverCon.clone());
		serverClass.stop();
		serverClass = newServerClass;
		newServerClass = null;
		pl("update completed");
	}
	
	public void calculateNextCheckTime(int minutes) {
		nextCheckTime = Calendar.getInstance();
		nextCheckTime.add(Calendar.MINUTE, minutes);
		pl("next checktime: " + calendarToString(nextCheckTime));
	}
	
	public void calculateNextCheckTime() {
		nextCheckTime = Calendar.getInstance();
		if ( nextCheckTime.get(Calendar.HOUR_OF_DAY) > 3)
			nextCheckTime.add(Calendar.DAY_OF_YEAR, 1);
		nextCheckTime.set(Calendar.HOUR_OF_DAY, 4);
		nextCheckTime.set(Calendar.MINUTE, 0);
		pl("next checktime: " + calendarToString(nextCheckTime));
	}
	
	public static void main(String[] args) {
		new Updater("1", 2001);
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
					errorIds.add(id);
					pl("serverversion " + id + " is not working");
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
			pl("failed to recive new id (probably no internet connection)");
			return -1;
		}
		int id = -1;
		try {
			id = Integer.parseInt(s);
		}
		catch(Exception e) {}
		pl((id == -1 ? "recive id is not an integer" : "new id recived: id = " + id));
		return id;
	}
	
	public int downloadFile() {
		File file;
		try {
			final URL url = new URL("http://dl.dropbox.com/u/89839665/Chatroom/Server.jar");
			final URLConnection conn = url.openConnection();
			final InputStream is = new BufferedInputStream(conn.getInputStream());
			final OutputStream os = new BufferedOutputStream(new FileOutputStream("Server.jar"));
			byte[] chunk = new byte[1024];
			int chunkSize;
			while ((chunkSize = is.read(chunk)) != -1) {
				os.write(chunk, 0, chunkSize);
			}
			os.close();
			is.close();
			file = ReadWriteFile.getFileInParentPath("Server.jar");
		}
		catch(Exception e) {
			pl("failed to get new jar-file (probably no internet connection)");
			return -2;
		}
		catch(Throwable e) {
			pl("failed to get new jar-file");
			pe(e);
			return -2;
		}
		try {
			URL url1 = file.toURI().toURL();
			ClassLoader loader = URLClassLoader.newInstance(new URL[] { url1 });
			Class<?> clazz = Class.forName("Server", true, loader);
			Class<? extends StartClass> strtClss = clazz.asSubclass(StartClass.class);
			newServerClass = strtClss.newInstance();
			pl("new jar-file downloaded");
			return 0;
		}
		catch(Exception e) {
			pl("failed to create new Instance");
			pe(e);
			return -1;
		}
		catch(Throwable e) {
			pl("failed to create new Instance");
			pe(e);
			return -2;
		}
	}
	
	public void pl(String s) {
		System.out.println( calendarToString(Calendar.getInstance()) + " Updater: " + s);
	}
	
	public String calendarToString(Calendar c) {
		int t = c.get(Calendar.DAY_OF_MONTH);
		String date = (t < 10 ? "0" + t : "" + t) + ".";
		t = c.get(Calendar.MONTH) + 1;
		date += (t < 10 ? "0" + t : "" + t) + ".";
		t = c.get(Calendar.YEAR);
		date += t + " ";
		t = c.get(Calendar.HOUR_OF_DAY);
		date += (t < 10 ? "0" + t : "" + t) + ":";
		t = c.get(Calendar.MINUTE);
		date += (t < 10 ? "0" + t : "" + t) + ":";
		t = c.get(Calendar.SECOND);
		date += (t < 10 ? "0" + t : "" + t);
		return date;
	}
	
	public void pe(Throwable e) {
		e.printStackTrace();
	}
	
}
