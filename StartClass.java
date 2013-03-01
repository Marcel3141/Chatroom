import Configuration.Configuration;

public class StartClass {
	
	public static StartClass start(String pw) { 
		return new StartClass();
	}
	
	public static StartClass start(Configuration con) {
		return new StartClass();
	}
	
	public Configuration getConfig() {
		return new Configuration();
	}
	
	public void pause() {}
	
	public void stop() {}
	
}
