package Verschluesselung;

import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * RSA ist eine statische Klasse zur asymetrischen Verschluesselung von Strings mit Hilfe von RSA.<br />
 * Diese Klasse enthaelt Methoden zum Ver- und Entschluesseln und zum generieren von den fuer RSA benoetigten Keys.<br />
 * @author Marcel Kramer
 * @serial 1.0
 * @since 1.6
 */
public class RSA {
	
	/*Konstanten die zum Rechnen benoetigt werden*/
	public final static BigInteger b1=new BigInteger("1");
	public final static BigInteger b0=new BigInteger("0");
	public final static BigInteger bm1=new BigInteger("-1");
		
	/**
	 * Erstellt die benoetigten Schluessel fuer asymetrische RSA-Verschluesselung. <br />
	 * <br />
	 * Da die Funktion mit wahrscheinlichen Primzahlen arbeitet kann es zu falschen Schluesseln kommen.<br />
	 * Wahrscheinlichkeit das die Primfaktoren keine Primzahlen sind ist ungefaehr 2 ^(-100).<br />
	 * Trotz falscher Schluessel funktioniert die Verschluesselung fast immer.<br />
	 * <br />
	 * binLaenge wird automatisch auf mindestens 20 gesetzt. Empfohlen wird eine Laenge von mindestens 1024 Bit<br />
	 * 
	 * @param binLaenge - binaere Laenge der Primfaktoren (empfolen:min. 1024)
	 * @return 	schluessel[0] - Produkt (oeffendlicher Schluessel 1)<br />
	 * 			schluessel[1] - Exponent (oeffendlicher Schluessel 2)<br />
	 * 			schluessel[2] - Inverses zu Exponent bezueglich Phi(Produkt) (geheimer Schluessel)
	 */
	public static BigInteger[] erstellen(int binLaenge) {
		BigInteger schluessel[] = new BigInteger[3];
		if (binLaenge < 20) binLaenge = 20; //binaere Laenge der Primfaktoren muss mindestens 20 sein
		BigInteger phi;
		BigInteger prim1=BigInteger.probablePrime(binLaenge, new Random()); //p
		BigInteger prim2=BigInteger.probablePrime(binLaenge, new Random()); //q
		schluessel[0]=prim1.multiply(prim2); //m=p*q
		phi=prim1.add(bm1).multiply(prim2.add(bm1)); //phi=(p-1)*(q-1)
		schluessel[1]=new BigInteger("65537"); //'zufaellige' Zahl e
		while (true) {	
			if (ggT(phi,schluessel[1]).equals(b1)) break; /*wenn e Teilerfremd zu phi ist, ist alles ok,*/
			schluessel[1] = schluessel[1].add(b1).add(b1); /*sonst wird e solange um 2 erhoeht, bis e Teilerfremd zu phi ist*/
		}
		try {
			schluessel[2]=Invers(schluessel[1], phi); //d berechnen: die diophantische Gleichung e * d + v * phi = 1 loesen
		}
		catch (NoSuchElementException e) {
			schluessel=erstellen(binLaenge); //falls es zu einem Fehler gekommen ist, der die Existenz von d ausschliesst, wird neu gerechnet
		}
		return schluessel; //Rueckgabe von schluessel = {m,e,d} & beenden der Methode
	}
	
	/**
	 * Verschluesselt den String klar mit dem asymetrischen Verfahren RSA <br />
	 * <br />
	 * Der String darf alle Zeichen aus dem Unicodeblock bis \uFFFF enthalten.<br />
	 * <br />
	 * rand_ziffern wird automatisch auf mindestens 2 gesetzt.<br />
	 * Wenn der String klar leer ist oder die maximale berechnete Laenge der Teiltexte
	 * (begrenzt durch die Laenge des Moduls und der Anzahl der zufaelligen Ziffern) 
	 * unter 1 ist, wird "0RQ" zurueck gegeben.<br />
	 * <br />
	 * "0RQ" ist immer der verschluesselte String ""
	 * 
	 * @param klar - Klartext
	 * @param produkt - Modul (oeffendlicher Schluessel 1)
	 * @param exponent - Exponent (oeffendlicher Schluessel 2)
	 * @param rand_ziffern - Anzahl zufaelliger Ziffern, die vor dem verschluesseln hinzugefuegt werden
	 * @return Kryptext
	 */
 	public static String chiffrieren(String klar, BigInteger produkt, BigInteger exponent, int rand_ziffern) {
		/* 
		 * Der zurueckgegebene Text hat folgende Struktur:
		 *		Anzahl der Zufaelligen Ziffern + 'R'
		 *		+ Laenge des Teilcryptext1 + 'L' + Teilcryptext1 
		 *		+ Laenge des Teilcryptext2 + 'L' + Teilcryptext2
		 *		...
		 *		+'Q'
		 */
		if (klar == null || produkt == null || exponent == null)
			return "0RQ";
 		String s="";
		String chiff=rand_ziffern+"R";
		if (klar == "") return "0RQ"; //wenn kein Text verschluesselt werden soll wird "0RQ" zurueck gegeben
		if (rand_ziffern < 2) rand_ziffern = 2; //die Anzahl zufaellig eingefuegter Ziffern sollte mindestenz 2 sein
		int laenge = (int) ( ( produkt.toString().length() - rand_ziffern - 1 ) / 5 ); //die maximale Anzahl Zeichen, die in einem Schritt verschluesselt werden koennen
		if (laenge < 1) return "0RQ";  //wenn aufgrund einer sinnlosen Eingabe die maximale Anzahl Zeichen, die in einem Schritt verschluesselt werden koennen unter 1 ist wird "" zurueck gegeben
		for ( int j = 0; j * laenge < klar.length(); j++ ) { //der Text wird in Teiltexte zerlegt mit der Laenge laenge so, dass laenge*5+zufaellige Ziffern < Laenge des Produktes(Schluessel 1)
			s = ( ( (int) (9 * Math.random() + 1.0 ) ) + "" ); //zufaellige Anfangsziffer einfuegen
			for ( int n = 0; n < rand_ziffern-1; n++ ) s += ( ( (int) ( 10 * Math.random() ) ) + "" );//weitere zufaellige Ziffern einfuegen
			/*
			 * Jedes Zeichen wird in eine Zahl mit 5 Dezimalstellen umgewandelt und um 25545 erhoeht und mod 65536 genommen. 
			 * Wenn ein Ergebnis weniger als 5 Dezimalstellen hat werden Nullen davorgesetzt.
			 * Dann werden die Zahlen hintereinander gehaengt und hinter die zufaelligen Anfangsziffern
			 */
			int wdh = Math.min(laenge, klar.length()-(laenge*j));
			for(int k=0;k<wdh;k++) {
				int cp=(klar.codePointAt(k+j*laenge)+25545)%65536;
				for (int i=(cp+"").length();i<5;i++) s+="0";
				s+=(cp+"");
			}
			s=new BigInteger(s).modPow(exponent, produkt).toString(); //Verschluesselung eines Teiltextes
			chiff+=(s.length()+"L"+s); //Formatierung
		}
		chiff+="Q"; //Formatierung
		return chiff; //Rueckgabe des entschluesselten Textes
	}
	
 	/**
 	 * Entschluesselt den String chiff mit dem asymetrischen Verfahren RSA<br />
 	 * Diese Methode ist das Gegenstueck zu chiffrieren.<br />
 	 * <br />
 	 * Wenn z.B. durch Uebertragungsfehler der Kryptext nicht zu entschluesseln ist, gibt es eine NumberFormatException.<br />
 	 * <br />
 	 * Der zu uebergebene Text braucht folgende Struktur um entschluesselt werden zu koennen: <br />
 	 *		Anzahl der Zufaelligen Ziffern + 'R' <br />
 	 *		+ Laenge des Teilcryptext1 + 'L' + Teilcryptext1 <br />
 	 *		+ Laenge des Teilcryptext2 + 'L' + Teilcryptext2 <br />
 	 *		... <br />
 	 *		+'Q' <br />
 	 * 
 	 * @param chiff - Kryptext
 	 * @param produkt - Modulo (oeffendlicher Schluessel 1)
 	 * @param inverses - Inverses zum Exponenten (geheimer Schluessel)
 	 * @throws NumberFormatException wenn Teile des Strings falsch formatiert sind, so dass nicht entschluesselt werden kann
 	 * @return Klartext
 	 */
	public static String dechiffrieren(String chiff, BigInteger produkt, BigInteger inverses) throws NumberFormatException {
		int j=0;
		String s="";
		int k;
		int rand;
		if (chiff == "") return "";
		while (chiff.charAt(j) != 'R') j++;
		rand = Integer.parseInt(chiff.substring(0,j)); //Anzahl der zufaelligen Ziffern
		j++;
		while (j < chiff.length() && chiff.charAt(j)!='Q') {
			k=j;
			while (j < chiff.length() && chiff.charAt(j) != 'L') j++;;
			k=Integer.parseInt(chiff.substring(k,j)); //Laenge des aktuellen Teiltextes
			j++;
			String b = new BigInteger( chiff.substring( j, j+k ) ).modPow( inverses, produkt ).toString(); //dechiffrieren
			/*
			 * Die zufaelligen Ziffern werden uebersprungen.
			 * Die Zahl wird zu Bloecken zu je 5 Ziffern geteilt.
			 * Jeder dieser Blocke entspricht einem Unicodezeichen.
			 */
			for ( int i = rand; i < b.length()-1; i += 5 ) {
				try {
					s += ( ( (char) ( ( Integer.parseInt( b.substring( i, i+5 ) ) - 25545 ) % 65536 ) ) + "" );
				}
				catch (StringIndexOutOfBoundsException e) {
					throw new NumberFormatException("Klartext kann nicht in Unicodezeichen zerlegt werden");
				}
			}
			j+=k;
		}
		return s; //Rueckgabe des entschluesselten Textes
	}
	
	/**
	 * Berechnet den groessten gemeinsamen Teiler von a und b rekursiv mit dem euklidischen Algorithmus
	 * @param a
	 * @param b
	 * @return ggT(a,b)
	 */
	public static BigInteger ggT(BigInteger a, BigInteger b) {
		/*a und b werden der groesse nach geortnet. Dann wird der euklidischen Algorithmus ausgefuehrt*/
		if (a.compareTo(b)>=0) return ggT2(a,b);
		else return ggT2(b,a);
	}
	
	/**
	 * Euklidischer Algorithmus<br />
	 * Hilfsmethode zum Berechnen des groessten gemeinsamen Teiler von a und b
	 * @param a
	 * @param b
	 * @return ggT(a,b)
	 */
	private static BigInteger ggT2(BigInteger a, BigInteger b) {
		if (b.equals(b0)) return a; //Abbruch der Rekursion
		else return ggT2(b,a.mod(b));//Rekursion (euklidischen Algorithmus)
	}
	
	/**
	 * Berechnet das Inverse zu a Modulo m mit Hilfe des erweiterten euklidischen Algorithmus <br />
	 * <br />
	 * a und m muessen Teilerfremd sein, damit a invertierbar ist. <br />
	 * 
	 * @param a
	 * @param m - Modul
	 * @return a<sup>-1</sup> - Inverses zu a
	 * @throws NoSuchElementException wenn a nicht invertierbar modulo m ist
	 */
	public static BigInteger Invers(BigInteger a, BigInteger m) throws NoSuchElementException {
		if (!ggT(a,m).equals(b1)) new NoSuchElementException(a.toString()+" ist nicht invertierbar modulo "+m.toString()); //generiert eine NoSuchElementException, wenn a nicht invertierbar modulo m ist
		return eeA(a.mod(m),m)[0].mod(m); //Rueckgabe des Inversen
	}
	
	/**
	 * Erweiterter euklidischen Algorithmus<br />
	 * Hilfsmethode um diophantische Gleichungen zu loesen
	 * @param a
	 * @param b
	 * @return 	eeA[0] - s <br />
	 * 			eeA[1] - t <br />
	 * 			ggT(a,b) = a*s+b*t
	 */
	public static BigInteger[] eeA(BigInteger a, BigInteger b) {
		BigInteger st[] = new BigInteger[2];
		if (a.mod(b).equals(b0)) { //Abbruchbedingung
			st[0]=b0;
			st[1]=b1;
			return st;
		} 
		BigInteger q=(BigInteger) (a.divide(b));
		BigInteger st2[] = eeA(b,a.mod(b)); //Rekursion (erweiterter euklidischen Algorithmus)
		st[0]=st2[1];
		st[1]=st2[0].add(q.multiply(st2[1]).negate());	
		return st; //Rueckgabe 
	}	
}