package Verschluesselung;

import java.util.Vector;
import java.math.BigInteger;

/**
 * Berechnet Schl&uuml;ssel zur asymetrischen Verschl&uuml;sselung
 * @author Marcel Kramer
 */
public class PrimzahlThread extends Thread {
	
	Vector<BigInteger[]> keys;
	int maxSize;
	int binLength;
	
	boolean enabled;
	
	public PrimzahlThread(Vector<BigInteger[]> keys, int maxSize, int binLength) {
		this.keys = keys;
		this.maxSize = maxSize;
		this.binLength = binLength;
		enabled = true;
		this.start();
	}
	
	public void stopThread() {
		enabled = false;
	}
	
	public void run() {
		while(enabled) {
			while (enabled && keys.size() < maxSize) {
				keys.add(RSA.erstellen(binLength));
			}
			try {
				Thread.sleep(5000);
			} 
			catch (InterruptedException e) {}
		}
	}	
}
