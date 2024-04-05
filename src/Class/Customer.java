package Class;

import java.util.List;
import java.util.Vector;

public class Customer extends User {
	private String name;
	private String phoneNumber;
	private List<Room> reservedRoomList;
	
	public Customer(String ID, String password, String name, String phoneNumber) {
		super(ID, password);
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.reservedRoomList = new Vector<Room>();
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public List<Room> getReservedRoomList() {
		return reservedRoomList;
	}
	public void setReservedRoomList(List<Room> reservedRoomList) {
		this.reservedRoomList = reservedRoomList;
	}

	public void addReservation(Room room) {
		this.reservedRoomList.add(room);
	}
	public void cancelReservation(Room room) {
		this.reservedRoomList.remove(room);
	}
}