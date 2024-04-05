package Protocol;

import java.io.Serializable;

public class Protocol<T> implements Serializable {
	private String message;
	private T object;
	
	public Protocol(String message, T object) {
		this.message = message;
		this.object = object;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}
}