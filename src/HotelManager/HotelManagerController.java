package HotelManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import Class.Customer;
import Class.DoubleRoom;
import Class.QuadRoom;
import Class.Room;
import Model.ReservationModel;
import Model.RoomModel;
import Protocol.Protocol;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class HotelManagerController implements Initializable {

	@FXML StackPane stackPane;
	
	@FXML AnchorPane reservationManagementPane;  //���� ���� ��ȸ ȭ��
	@FXML TableView<ReservationModel> reservationTableView;
	@FXML TableColumn<ReservationModel, String> customerNameColumn;
	@FXML TableColumn<ReservationModel, String> phoneNumberColumn;
	@FXML TableColumn<ReservationModel, String> customerIDColumn;
	@FXML TableColumn<ReservationModel, String> customerPasswordColumn;
	@FXML TableColumn<ReservationModel, String> reservedRoomNumberColumn;
	@FXML TextField customerSearchField;
	
	@FXML AnchorPane roomManagementPane;  //������ �߰��ϰų� ������ �� �ִ� ���� ���� ȭ��
	@FXML TableView<RoomModel> availableRoomTableView;
	@FXML TableColumn<RoomModel, String> availableRoomNumberColumn;
	@FXML TableColumn<RoomModel, String> availableRoomTypeColumn;
	@FXML TableColumn<RoomModel, String> availableRoomOriginalPriceColumn;
	@FXML TableColumn<RoomModel, String> availableRoomReducedPriceColumn;
	@FXML TableColumn<RoomModel, String> availableRoomReservationPeriodColumn;
	@FXML DatePicker availableRoomInDatePicker;
	@FXML DatePicker availableRoomOutDatePicker;
	@FXML TextField availableRoomNumberField;
	@FXML TextField availableRoomTypeField;
	@FXML TextField availableRoomOriginalPriceField;
	@FXML TextField availableRoomReducedPriceField;
	
	@FXML AnchorPane managementSelectionPane;  //������ ��ȸ�ϰų� ���� ������ �����ϴ� ȭ��
	@FXML ToggleGroup managementSelection;
	
	@FXML AnchorPane managerLoginPane;  //������ �α��� ȭ��
	@FXML TextField hotelNameField;
	@FXML PasswordField passwordField;
	
	Socket socket;
	String ID;
	List<RoomModel> roomModelList = FXCollections.observableArrayList();  //���� �̿� ������ ���� ���
	List<ReservationModel> reservationModelList = FXCollections.observableArrayList();  //�ش� ȣ���� ������ ���� ���
	FilteredList<ReservationModel> filteredList = new FilteredList<ReservationModel>((ObservableList<ReservationModel>) reservationModelList);  //������ ����� �����ؼ� �����ִ� ���
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {  //�ش� ���̺� �信 ����� �Ҵ�
		reservationTableView.setPlaceholder(new Label("���� �� ����"));
		reservationTableView.setItems((ObservableList<ReservationModel>) reservationModelList);
		customerNameColumn.setCellValueFactory(param -> param.getValue().getCustomerNameProperty());
		phoneNumberColumn.setCellValueFactory(param -> param.getValue().getPhoneNumberProperty());
		customerIDColumn.setCellValueFactory(param -> param.getValue().getCustomerIDProperty());
		customerPasswordColumn.setCellValueFactory(param -> param.getValue().getCustomerPasswordProperty());
		reservedRoomNumberColumn.setCellValueFactory(param -> param.getValue().getRoomNumberProperty());
		
		availableRoomTableView.setPlaceholder(new Label("���� ����"));
		availableRoomTableView.setItems((ObservableList<RoomModel>) roomModelList);
		availableRoomNumberColumn.setCellValueFactory(param -> param.getValue().getRoomNumberProperty());
		availableRoomTypeColumn.setCellValueFactory(param -> param.getValue().getRoomTypeProperty());
		availableRoomOriginalPriceColumn.setCellValueFactory(param -> param.getValue().getOriginalPriceProperty());
		availableRoomReducedPriceColumn.setCellValueFactory(param -> param.getValue().getReducedPriceProperty());
		availableRoomReservationPeriodColumn.setCellValueFactory(param -> param.getValue().getReservationPeriodProperty());
		
		startClient();  //Ŭ���̾�Ʈ ����
	}
	
	void startClient() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                	socket = new Socket();  //���� ���� �� ����
                    socket.connect(new InetSocketAddress("localhost", 7777));
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                byte[] bytes = "Manager".getBytes(StandardCharsets.UTF_8);  //�������̱� ������ �����ڶ�� ���� �˸��� ���� Manager�� ����
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
                                bufferedOutputStream.write(bytes);
                                bufferedOutputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                                disconnectServer();
                            }
                        }
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();
                    
                    try {
                    	thread.join();
                    } catch (InterruptedException e) {
                    	e.printStackTrace();
                    }
                } catch (IOException e) {
                    disconnectServer();
                }
                receive();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
    void receive() {  //��û�� ���� ������ �޾Ƽ� ó���ϴ� �޼ҵ�
        while (true) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                Protocol protocol = (Protocol) objectInputStream.readObject();
                
                if (protocol.getMessage().contentEquals("noAccount")) {  //������ ���� ���
                	Platform.runLater(() -> {
                		new Alert(Alert.AlertType.WARNING, "������ �����ϴ�.", ButtonType.CLOSE).show();
                	});
                }
                
                if (protocol.getMessage().contentEquals("existAccount")) {  //������ �ִ� ���
                	String password = (String) protocol.getObject();
                	if (password.contentEquals(passwordField.getText())) {
            			Platform.runLater(() -> {
            				Alert alert = new Alert(Alert.AlertType.INFORMATION, "�α��ο� �����ϼ̽��ϴ�.", ButtonType.CLOSE);
                			alert.showAndWait();
                			stackPane.getChildren().remove(managerLoginPane);
                			hotelNameField.setText("");
                			passwordField.setText("");
            			});
            		}
                	else {
                		Platform.runLater(() -> {
                			new Alert(Alert.AlertType.ERROR, "��й�ȣ�� ��ġ���� �ʽ��ϴ�.", ButtonType.CLOSE).show();
                		});
                	}
                }
                
                if (protocol.getMessage().contentEquals("returnRoomList")) {  //���� �̿� ������ ���� ��� ��ȸ
                	List<Room> roomList = (List<Room>) protocol.getObject();
                	roomModelList.clear();
                	for (int i=0; i<roomList.size(); i++) {
                		roomModelList.add(new RoomModel(roomList.get(i).getHotelName(), roomList.get(i).getRoomNumber(), roomList.get(i).getRoomType(), roomList.get(i).getOriginalPrice(), roomList.get(i).getReducedPrice(), roomList.get(i).getReservationPeriod()));
                	}
                }
                
                if (protocol.getMessage().contentEquals("returnCustomerList")) {  //������ ���� ������ ������
                	List<Customer> list = (List<Customer>) protocol.getObject();
                	reservationModelList.clear();
        			for (int i=0; i<list.size(); i++) {
            			for (int j=0; j<list.get(i).getReservedRoomList().size(); j++) {
            				if (list.get(i).getReservedRoomList().get(j).getHotelName().contentEquals(ID))
            					reservationModelList.add(new ReservationModel(list.get(i).getID(), list.get(i).getPassword(), list.get(i).getName(), list.get(i).getPhoneNumber(), list.get(i).getReservedRoomList().get(j).getRoomNumber()));
            			}
            		}
                }
                
                if (protocol.getMessage().contentEquals("deleteAvailableRoom")) {  //�ܺο��� ��ȭ ���� ������ ���� ������ �� ���
                	Room room = (Room) protocol.getObject();                       //�ش� ���� �����ϸ� �������� �����ϰ� �� Ŭ���̾𿡵� �ǽð�����
                	for (int i=0; i<roomModelList.size(); i++) {                   //�ݿ��ϵ��� �ϱ� ���� ������ ��û ����
                		if (roomModelList.get(i).getHotelName().contentEquals(room.getHotelName())&&roomModelList.get(i).getRoomNumber().contentEquals(room.getRoomNumber()))
                			roomModelList.remove(i);
                	}
                	send("getCustomerList", ID);
                }
                
                if (protocol.getMessage().contentEquals("addReservedRoom")) {  //������ ��ҵ� ���� �߰�
                	Room room = (Room) protocol.getObject();
                	roomModelList.add(new RoomModel(room.getHotelName(), room.getRoomNumber(), room.getRoomType(), room.getOriginalPrice(), room.getReducedPrice(), room.getReservationPeriod()));
                	send("getCustomerList", ID);
                }
            } catch (IOException e) {
                disconnectServer();
                break;
            } catch (ClassNotFoundException e) {
            	disconnectServer();
            	break;
            }
        }
    }
    
    <T> void send(String message, T object) {  //��û�� ������ ������ �޼ҵ�
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    objectOutputStream.writeObject(new Protocol<T>(message, object));
                    objectOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnectServer();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        
        try {
        	thread.join();
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
    }

    void disconnectServer() {  //�������� ������ ���� �޼ҵ�
    	if (socket != null && !socket.isClosed()) {
			 try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		 }
    }
	
	// reservationManagementPane
	@FXML public void moveFromResrvationPaneToSelectionPane() {  //������ �����ϴ� ȭ������ ���ư���
		customerSearchField.setText("");
		reservationTableView.setItems((ObservableList<ReservationModel>) reservationModelList);
		stackPane.getChildren().add(roomManagementPane);
		stackPane.getChildren().add(managementSelectionPane);
	}
	@FXML public void searchAction() {  //���� ��� �� �˻�� ������ �̸��� ���� ����� ������
		filteredList.setPredicate(new Predicate<ReservationModel>() {
			@Override
			public boolean test(ReservationModel t) {
				if (t.getCustomerName().contains(customerSearchField.getText()))
					return true;
				return false;
			}
		});
		reservationTableView.setItems(filteredList);
	}
	@FXML public void quitAction2() {  //�ý��� ����
		quitAction1();
	}
	
	// roomManagementPane
	@FXML public void addAvailableRoomAction() {  //�̿� ������ ������ �߰��ϱ�
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				String roomNumber = availableRoomNumberField.getText();
				String roomType = availableRoomTypeField.getText();
				String originalPrice = availableRoomOriginalPriceField.getText();
				String reducedPrice = availableRoomReducedPriceField.getText();
				String reservationPeriod = availableRoomInDatePicker.getValue()+"~"+availableRoomOutDatePicker.getValue();
				
				Room room = null;
				if (roomType.contentEquals("2�ν�")) {  //2�ν��� ���
					room = new DoubleRoom(ID, roomNumber, originalPrice, reducedPrice, reservationPeriod);
				}
				else {  //4�ν��� ���
					room = new QuadRoom(ID, roomNumber, originalPrice, reducedPrice,reservationPeriod);
				}
				send("addAvailableRoom", room);  //�̿� ������ ���� �߰��Ǿ��ٴ� ���� ������ �˷� �����Ŵ
				Platform.runLater(() -> {
					roomModelList.add(new RoomModel(ID, roomNumber, roomType, originalPrice, reducedPrice, reservationPeriod));
					availableRoomNumberField.setText("");
					availableRoomTypeField.setText("");
					availableRoomOriginalPriceField.setText("");
					availableRoomReducedPriceField.setText("");
					availableRoomInDatePicker.setValue(null);
					availableRoomOutDatePicker.setValue(null);
				});
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	@FXML public void deleteReservedRoomAction() {  //�� ���α׷��� ������� �ʰ� ���� �� ������ ����
		RoomModel roomModel = availableRoomTableView.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "������ �����Ͻðڽ��ϱ�?", ButtonType.OK, ButtonType.CANCEL);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get()==ButtonType.OK) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					send("deleteReservedRoom", roomModel.getRoomNumber());
					Platform.runLater(() -> {
						new Alert(Alert.AlertType.INFORMATION, "������ �����Ǿ����ϴ�.", ButtonType.CLOSE).show();
						roomModelList.remove(roomModel);						
					});
				}
			};
			Thread t = new Thread(runnable);
			t.start();
		}
	}
	@FXML public void moveFromRoomPaneToSelectionPane() {
		stackPane.getChildren().add(managementSelectionPane);
		availableRoomNumberField.setText("");
		availableRoomTypeField.setText("");
		availableRoomOriginalPriceField.setText("");
		availableRoomReducedPriceField.setText("");
		availableRoomInDatePicker.setValue(null);
		availableRoomOutDatePicker.setValue(null);
	}
	@FXML public void quitAction1() {  //�ý��� ����
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				send("quit", "");  //������ ����Ǿ��ٴ� ���� �˸�
				disconnectServer();  //���� ������ ����
				System.exit(0);  //�ý��� ����
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	
	// managementSelectionPane
	@FXML public void checkAction() {
		if (managementSelection.getSelectedToggle()!=null) {  //���� ���� ȭ�鿡�� ������ �Ǿ��� ���
			if (managementSelection.getSelectedToggle().getUserData().toString().contentEquals("roomManagement")) {  //�� ������ ���
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						send("getRoomList", ID);
						Platform.runLater(() -> {
							stackPane.getChildren().remove(managementSelectionPane);
						});
					}
				};
				Thread t = new Thread(runnable);
				t.start();
			}
			else if (managementSelection.getSelectedToggle().getUserData().toString().contentEquals("reservationManagement")) {  //���� ��� ��ȸ�� ���
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						send("getCustomerList", ID);
						Platform.runLater(() -> {
							stackPane.getChildren().remove(managementSelectionPane);
							stackPane.getChildren().remove(roomManagementPane);
						});
					}
				};
				Thread t = new Thread(runnable);
				t.start();
			}
		}
	}
	
	// managerLoginPane
	@FXML public void loginAction() { //������ �α���
		ID = hotelNameField.getText();
		String password = passwordField.getText();
		
		if (ID.equals("")) {
            new Alert(Alert.AlertType.WARNING, "���̵� �Է��ϼ���.", ButtonType.CLOSE).show();
            hotelNameField.requestFocus();
            return;
        }
        if (password.equals("")) {
            new Alert(Alert.AlertType.WARNING, "��й�ȣ�� �Է��ϼ���.", ButtonType.CLOSE).show();
            passwordField.requestFocus();
            return;
        }
        
        Runnable runnable = new Runnable() {
			@Override
			public void run() {
				send("checkAccount", ID);
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
}