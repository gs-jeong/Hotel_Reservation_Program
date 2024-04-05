package Class;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class Hotel implements Serializable {
	private String hotelName;
	private List<Room> roomList;
	
	public Hotel(String hotelName) {
		this.hotelName = hotelName;
		roomList = new Vector<Room>();
	}
	
	public String getHotelName() {
		return hotelName;
	}
	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}
	
	public List<Room> getRoomList() {
		return roomList;
	}
	public void setRoomList(List<Room> roomList) {
		this.roomList = roomList;
	}
}