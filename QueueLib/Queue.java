package QueueLib;

public class Queue<T> {

	Element<T> last;
  
	public Queue() {
		last=null;
	}

	public void enqueue(T inhalt) {
		if(isEmpty()) {
			last = new Element<T>(inhalt);
		}
		else {
			Element<T> tmp = last;
			while(tmp.priv != null) {
				tmp = tmp.priv;
			}
			tmp.priv = new Element<T>(inhalt);
		}
	}

	public boolean isEmpty() {
		return last==null;
	}

	public void dequeue() {
		if (!isEmpty()) last=last.priv;
	}

	public T head() {
		if (isEmpty()) return null;
		return last.inhalt;
	}
}