package ReservationClient;

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
import Class.DoubleRoom;
import Class.QuadRoom;
import Class.Room;
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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class ReservationClientController implements Initializable {
	
	@FXML StackPane stackPane;
	
	@FXML AnchorPane checkReservationPane;  //���� ��ȸ ȭ��
	@FXML TableView<RoomModel> reservedRoomTableView;  //������ ���� ǥ���ϴ� ���̺� ��
	@FXML TableColumn<RoomModel, String> reservedHotelNameColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomNumberColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomTypeColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomOriginalPriceColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomReducedPriceColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomReservationPeriodColumn;
	
	@FXML AnchorPane reservationPane;  //���� �����ϴ� ȭ��
	@FXML TableView<RoomModel> availableRoomTableView;  //���� ������ ���� ǥ���ϴ� ���̺� ��
	@FXML TableColumn<RoomModel, String> availableHotelNameColumn;
	@FXML TableColumn<RoomModel, String> availableRoomNumberColumn;
	@FXML TableColumn<RoomModel, String> availableRoomTypeColumn;
	@FXML TableColumn<RoomModel, String> availableRoomOriginalPriceColumn;
	@FXML TableColumn<RoomModel, String> availableRoomReducedPriceColumn;
	@FXML TableColumn<RoomModel, String> availableRoomReservationPeriodColumn;
	@FXML TextField searchField;
	
	@FXML AnchorPane loginPane;  //�� �α��� ȭ��
	@FXML TextField IDField;
	@FXML PasswordField passwordField;
	
	Socket socket;
	String ID;
	List<RoomModel> roomModelList = FXCollections.observableArrayList();  //���� ������ �� ����Ʈ
	FilteredList<RoomModel> filteredList = new FilteredList<RoomModel>((ObservableList<RoomModel>) roomModelList);  //�˻� �� ���� ������ ���� ����� �����ؼ� �����ִ� ����Ʈ
	List<RoomModel> reservedRoomModelList = FXCollections.observableArrayList();  //������ �� ����Ʈ
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {  //����Ʈ���� �ش��ϴ� ���̺� �ο� �Ҵ�
		availableRoomTableView.setPlaceholder(new Label("���� ����"));
		availableRoomTableView.setItems((ObservableList<RoomModel>) roomModelList);
		availableHotelNameColumn.setCellValueFactory(param -> param.getValue().getHotelNameProperty());
		availableRoomNumberColumn.setCellValueFactory(param -> param.getValue().getRoomNumberProperty());
		availableRoomTypeColumn.setCellValueFactory(param -> param.getValue().getRoomTypeProperty());
		availableRoomOriginalPriceColumn.setCellValueFactory(param -> param.getValue().getOriginalPriceProperty());
		availableRoomReducedPriceColumn.setCellValueFactory(param -> param.getValue().getReducedPriceProperty());
		availableRoomReservationPeriodColumn.setCellValueFactory(param -> param.getValue().getReservationPeriodProperty());
		
		reservedRoomTableView.setPlaceholder(new Label("���� ��� ����"));
		reservedRoomTableView.setItems((ObservableList<RoomModel>) reservedRoomModelList);
		reservedHotelNameColumn.setCellValueFactory(param -> param.getValue().getHotelNameProperty());
		reservedRoomNumberColumn.setCellValueFactory(param -> param.getValue().getRoomNumberProperty());
		reservedRoomTypeColumn.setCellValueFactory(param -> param.getValue().getRoomTypeProperty());
		reservedRoomOriginalPriceColumn.setCellValueFactory(param -> param.getValue().getOriginalPriceProperty());
		reservedRoomReducedPriceColumn.setCellValueFactory(param -> param.getValue().getReducedPriceProperty());
		reservedRoomReservationPeriodColumn.setCellValueFactory(param -> param.getValue().getReservationPeriodProperty());
		
		startClient();  //Ŭ���̾�Ʈ ����
	}
	
	void startClient() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                	socket = new Socket();  //������ ����� ����
                    socket.connect(new InetSocketAddress("localhost", 7777));
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                byte[] bytes = "Customer".getBytes(StandardCharsets.UTF_8);  //�� Ŭ���̾�Ʈ�̱� ������ ���̶�� �޼����� ���� �������� �� Ŭ���̾�Ʈ�� ������ �㵵�� ����
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
                                bufferedOutputStream.write(bytes);  //Customer�� ������ ����
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
                receive();  //��û�� �޾Ƶ��̴� �޼ҵ�
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
    void receive() {  //��û�� ���� ������ ��� �޾Ƶ��̴� �޼ҵ�
        while (true) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                Protocol protocol = (Protocol) objectInputStream.readObject();  //protocol�� ���� �޼����� ���� �ٸ� ���� ����
                
                if (protocol.getMessage().contentEquals("noAccount")) {  //�α��ν� ������ ���� ���
                	Platform.runLater(() -> {
                		new Alert(Alert.AlertType.WARNING, "������ �����ϴ�.", ButtonType.CLOSE).show();
                	});
                }
                
                if (protocol.getMessage().contentEquals("existAccount")) {  //�α��ν� ������ �ִ� ���
                	String password = (String) protocol.getObject();
                	if (password.contentEquals(passwordField.getText())) {  //��й�ȣ�� ��ġ�� ���
            			Platform.runLater(() -> {
            				Alert alert = new Alert(Alert.AlertType.INFORMATION, "�α��ο� �����ϼ̽��ϴ�.", ButtonType.CLOSE);
                			alert.showAndWait();
                			stackPane.getChildren().remove(loginPane);
                			IDField.setText("");
                			passwordField.setText("");
                			send("getRoomList", "");
            			});
            		}
                	else {
                		Platform.runLater(() -> {  //��й�ȣ�� Ʋ�� ���
                			new Alert(Alert.AlertType.ERROR, "��й�ȣ�� ��ġ���� �ʽ��ϴ�.", ButtonType.CLOSE).show();
                		});
                	}
                }
                
                if (protocol.getMessage().contentEquals("returnRoomList")) {  //�̿� ������ �� ����� ��������
                	List<Room> roomList = (List<Room>) protocol.getObject();
                	for (int i=0; i<roomList.size(); i++) {
                		roomModelList.add(new RoomModel(roomList.get(i).getHotelName(), roomList.get(i).getRoomNumber(), roomList.get(i).getRoomType(), roomList.get(i).getOriginalPrice(), roomList.get(i).getReducedPrice(), roomList.get(i).getReservationPeriod()));
                	}
                }
                
                if (protocol.getMessage().contentEquals("returnReservedRoomList")) {  //������ �� ����� ��������
                	List<Room> reservedRoomList = (List<Room>) protocol.getObject();
                	reservedRoomModelList.clear();
        			for (int i=0; i<reservedRoomList.size(); i++) {
            			reservedRoomModelList.add(new RoomModel(reservedRoomList.get(i).getHotelName(), reservedRoomList.get(i).getRoomNumber(), reservedRoomList.get(i).getRoomType(), reservedRoomList.get(i).getOriginalPrice(), reservedRoomList.get(i).getReducedPrice(), reservedRoomList.get(i).getReservationPeriod()));
            		}
                }
                
                if (protocol.getMessage().contentEquals("deleteAvailableRoom")) {  //������ ���� ��� �̿밡���� �� ��Ͽ��� �ش� ���� ����
                	Room room = (Room) protocol.getObject();
                	for (int i=0; i<roomModelList.size(); i++) {
                		if (roomModelList.get(i).getHotelName().contentEquals(room.getHotelName())&&roomModelList.get(i).getRoomNumber().contentEquals(room.getRoomNumber()))
                			roomModelList.remove(i);
                	}
                }
                
                if (protocol.getMessage().contentEquals("addReservedRoomToAvailableRoomList")) {  //���� ��Ҹ� �� ��� �ش� ���� �̿밡���� �� ��Ͽ� �߰�
                	Room room = (Room) protocol.getObject();
                	for (int i=0; i<reservedRoomModelList.size(); i++) {
                		if (reservedRoomModelList.get(i).getHotelName().contentEquals(room.getHotelName())&&reservedRoomModelList.get(i).getRoomNumber().contentEquals(room.getRoomNumber())) {
                			reservedRoomModelList.remove(i);
                		}
                	}
                	roomModelList.add(new RoomModel(room.getHotelName(), room.getRoomNumber(), room.getRoomType(), room.getOriginalPrice(), room.getReducedPrice(), room.getReservationPeriod()));
                }
                
                if (protocol.getMessage().contentEquals("deleteReservedRoom")) {  //�� ���α׷��� ������� �ʰ� �ܺ� ������ ���� ���� ������ ���
                	Room room = (Room) protocol.getObject();                      //�����ڰ� �ش� ���� �����ϸ� �� Ŭ���̾�Ʈ������ ����
                	for (int i=0; i<roomModelList.size(); i++) {
                		if (roomModelList.get(i).getHotelName().contentEquals(room.getHotelName())&&roomModelList.get(i).getRoomNumber().contentEquals(room.getRoomNumber()))
                			roomModelList.remove(i);
                	}
                }
                
                if (protocol.getMessage().contentEquals("addAvailableRoom")) {  //�����ڰ� �̿� ������ ���� �߰��ϸ�
                	Room room = (Room) protocol.getObject();                    //�� Ŭ���̾�Ʈ�� ǥ��
                	roomModelList.add(new RoomModel(room.getHotelName(), room.getRoomNumber(), room.getRoomType(), room.getOriginalPrice(), room.getReducedPrice(), room.getReservationPeriod()));
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
    
    <T> void send(String message, T object) {  //������ � ��û������ �޼����� �׿� �ʿ��� ��ü�� ����� ������ �޼ҵ�
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
	
	// checkReservationPane
	@FXML public void cancelReservationAction() {  //���� ��� �޼ҵ�
		RoomModel roomModel = reservedRoomTableView.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "������ ����Ͻðڽ��ϱ�?", ButtonType.OK, ButtonType.CANCEL);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get()==ButtonType.OK) {  //���� ��Ҹ� OK��ư���� Ȯ������ ��
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Room room = null;
					if (roomModel.getRoomType().contentEquals("2�ν�")) {  //2�ν��� ��� 2�ν� ��ü�� ����
						room = new DoubleRoom(roomModel.getHotelName(), roomModel.getRoomNumber(), roomModel.getOriginalPrice(), roomModel.getReducedPrice(), roomModel.getReservationPeriod());
					}
					else {  //4�ν��� ��� 4�ν� ��ü ����
						room = new QuadRoom(roomModel.getHotelName(), roomModel.getRoomNumber(), roomModel.getOriginalPrice(), roomModel.getReducedPrice(), roomModel.getReservationPeriod());
					}
					send("cancelReservation", room);  //������ �ش� ���� ������ ��ҵǾ��ٴ� ���� �˸�
				}
			};
			Thread t = new Thread(runnable);
			t.start();
		}
	}
	@FXML public void moveToPreviousPageAction() {  //���� ȭ������ ���ư��� �޼ҵ�
		stackPane.getChildren().add(reservationPane);
	}
	@FXML public void quitAction2() {  //�� Ŭ���̾�Ʈ ���� �޼ҵ�
		quitAction1();
	}
	
	// reservationPane
	@FXML public void reserveAvailableRoomAction() {  //�̿� ������ �� ���� �޼ҵ�
		RoomModel roomModel = availableRoomTableView.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "�����Ͻðڽ��ϱ�?", ButtonType.OK, ButtonType.CANCEL);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get()==ButtonType.OK) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Room room = null;
					if (roomModel.getRoomType().contentEquals("2�ν�")) {  //�ش��ϴ� ��ü ����
						room = new DoubleRoom(roomModel.getHotelName(), roomModel.getRoomNumber(), roomModel.getOriginalPrice(), roomModel.getReducedPrice(), roomModel.getReservationPeriod());
					}
					else {
						room = new QuadRoom(roomModel.getHotelName(), roomModel.getRoomNumber(), roomModel.getOriginalPrice(), roomModel.getReducedPrice(), roomModel.getReservationPeriod());
					}
					send("addReservation", room);  //�ش� ���� ����Ǿ��ٴ� ���� �˸�
				}
			};
			Thread t = new Thread(runnable);
			t.start();
		}
	}
	@FXML public void checkReservationAction() {  //���� Ȯ�� �޼ҵ�
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				send("getReservedRoomList", "");  //����� ���� ����� �������� �������� ���� ������ ��û
				Platform.runLater(() -> {
					stackPane.getChildren().remove(reservationPane);  //����Ȯ�� ȭ������ �̵�
				});
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	@FXML public void searchAction() {  //�̿� ������ �� �� ȣ���̸��� �˻����� ������ �����ϰ� �ִ� ȣ���� ���ĵ��� ������
		filteredList.setPredicate(new Predicate<RoomModel>() {
			@Override
			public boolean test(RoomModel t) {
				if (t.getHotelName().contains(searchField.getText()))
					return true;
				return false;
			}
		});
		availableRoomTableView.setItems(filteredList);
	}
	@FXML public void quitAction1() {  //�� Ŭ���̾�Ʈ ���� �޼ҵ�
		Runnable runnable = new Runnable() {	
			@Override
			public void run() {
				send("quit", "");  //������ �����ϰڴٴ� ��û�� ����
				disconnectServer();  //���� ������ ����
				System.exit(0);  //�ý��� ����
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	
	// loginPane
	@FXML public void loginAction() {  //�� �α���
		ID = IDField.getText();
        String password = passwordField.getText();
        
		if (ID.equals("")) {
            new Alert(Alert.AlertType.WARNING, "���̵� �Է��ϼ���.", ButtonType.CLOSE).show();
            IDField.requestFocus();
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
				send("checkAccount", ID);  //������ ������ Ȯ��
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
}