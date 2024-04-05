package Class;

import java.io.Serializable;

abstract public class Room implements Serializable {
	private String hotelName;
	private String roomNumber;
	private String originalPrice;
	private String reducedPrice;
	private String roomType;
	private String reservationPeriod;
	
	public Room(String hotelName, String roomNumber, String roomType, String originalPrice, String reducedPrice, String reservationPeriod) {
		this.hotelName = hotelName;
		this.roomNumber = roomNumber;
		this.roomType = roomType;
		this.originalPrice = originalPrice;
		this.reducedPrice = reducedPrice;
		this.reservationPeriod = reservationPeriod;
	}
	
	public String getHotelName() {
		return hotelName;
	}
	public void setHotelName(String hotelName) {
		this.hotelName = hotelName;
	}

	public String getRoomNumber() {
		return roomNumber;
	}
	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}
	
	public String getRoomType() {
		return roomType;
	}
	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}

	public String getOriginalPrice() {
		return originalPrice;
	}
	public void setOriginalPrice(String originalPrice) {
		this.originalPrice = originalPrice;
	}

	public String getReducedPrice() {
		return reducedPrice;
	}
	public void setReducedPrice(String reducedPrice) {
		this.reducedPrice = reducedPrice;
	}
	
	public String getReservationPeriod() {
		return reservationPeriod;
	}
	public void setReservationPeriod(String reservationPeriod) {
		this.reservationPeriod = reservationPeriod;
	}
}