package Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RoomModel {
	private StringProperty hotelName;
	private StringProperty roomNumber;
	private StringProperty originalPrice;
	private StringProperty reducedPrice;
	private StringProperty roomType;
	private StringProperty reservationPeriod;
	
	public RoomModel (String hotelName, String roomNumber, String roomType, String originalPrice,String reducedPrice, String reservationPeriod) {
		this.hotelName = new SimpleStringProperty(hotelName);
		this.roomNumber = new SimpleStringProperty(roomNumber);
		this.roomType = new SimpleStringProperty(roomType);
		this.originalPrice = new SimpleStringProperty(originalPrice);
		this.reducedPrice = new SimpleStringProperty(reducedPrice);
		this.reservationPeriod = new SimpleStringProperty(reservationPeriod);
	}
	
	public String getHotelName() {
		return hotelName.get();
	}
	public void setHotelName(String hotelName) {
		this.hotelName.set(hotelName);
	}
	public StringProperty getHotelNameProperty() {
		return hotelName;
	}
	
	public String getRoomNumber() {
		return roomNumber.get();
	}
	public void setRoomNumber(String roomNumber) {
		this.roomNumber.set(roomNumber);
	}
	public StringProperty getRoomNumberProperty() {
		return roomNumber;
	}
	
	public String getRoomType() {
		return roomType.get();
	}
	public void setRoomType(String roomType) {
		this.roomType.set(roomType);
	}
	public StringProperty getRoomTypeProperty() {
		return roomType;
	}
	
	public String getOriginalPrice() {
		return originalPrice.get();
	}
	public void setOriginalPrice(String originalPrice) {
		this.originalPrice.set(originalPrice);
	}
	public StringProperty getOriginalPriceProperty() {
		return originalPrice;
	}
	
	public String getReducedPrice() {
		return reducedPrice.get();
	}
	public void setReducedPrice(String reducedRrice) {
		this.reducedPrice.set(reducedRrice);
	}
	public StringProperty getReducedPriceProperty() {
		return reducedPrice;
	}
	
	public String getReservationPeriod() {
		return reservationPeriod.get();
	}
	public void setReservationPeriod(String reservationPeriod) {
		this.reservationPeriod.set(reservationPeriod);
	}
	public StringProperty getReservationPeriodProperty() {
		return reservationPeriod;
	}
}