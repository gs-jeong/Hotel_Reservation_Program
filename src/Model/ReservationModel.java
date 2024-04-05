package Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ReservationModel {
	private StringProperty customerID;
	private StringProperty customerPassword;
	private StringProperty customerName;
	private StringProperty phoneNumber;
	private StringProperty roomNumber;
	
	public ReservationModel(String customerID, String customerPassword ,String customerName, String phoneNumber, String roomNumber) {
		this.customerID = new SimpleStringProperty(customerID);
		this.customerPassword = new SimpleStringProperty(customerPassword);
		this.customerName = new SimpleStringProperty(customerName);
		this.phoneNumber = new SimpleStringProperty(phoneNumber);
		this.roomNumber = new SimpleStringProperty(roomNumber);
	}
	
	public String getCustomerID() {
		return customerID.get();
	}
	public void setCustomerID(String customerID) {
		this.customerID.set(customerID);
	}
	public StringProperty getCustomerIDProperty() {
		return customerID;
	}
	
	public String getCustomerPassword() {
		return customerPassword.get();
	}
	public void setCustomerPassword(String customerPassword) {
		this.customerPassword.set(customerPassword);
	}
	public StringProperty getCustomerPasswordProperty() {
		return customerPassword;
	}
	
	public String getCustomerName() {
		return customerName.get();
	}
	public void setCustomerName(String customerName) {
		this.customerName.set(customerName);
	}
	public StringProperty getCustomerNameProperty() {
		return customerName;
	}
	
	public String getPhoneNumber() {
		return phoneNumber.get();
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber.set(phoneNumber);
	}
	public StringProperty getPhoneNumberProperty() {
		return phoneNumber;
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
}