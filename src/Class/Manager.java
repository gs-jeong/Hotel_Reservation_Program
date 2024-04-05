package Class;

public class Manager extends User {
	private Hotel hotel;
	
	public Manager(String ID, String password, Hotel hotel) {
		super(ID, password);
		this.hotel = hotel;
	}
	
	public Hotel getHotel() {
		return hotel;
	}
	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}
	
	public void deleteRoom(Room room) {
		hotel.getRoomList().remove(room);
	}
	
	public void addRoom(Room room) {
		hotel.getRoomList().add(room);
	}
}