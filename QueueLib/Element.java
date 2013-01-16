package QueueLib;

class Element<T> {
	 T inhalt;
	 Element<T> priv;

	  public Element(T inhalt) {
	    this.inhalt=inhalt;
	    this.priv=null;
	  }

	  public Element(T inhalt, Element<T> priv) {
	    this.inhalt=inhalt;
	    this.priv=priv;
	  }
}