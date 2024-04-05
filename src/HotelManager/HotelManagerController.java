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
	
	@FXML AnchorPane reservationManagementPane;  //예약 고객을 조회 화면
	@FXML TableView<ReservationModel> reservationTableView;
	@FXML TableColumn<ReservationModel, String> customerNameColumn;
	@FXML TableColumn<ReservationModel, String> phoneNumberColumn;
	@FXML TableColumn<ReservationModel, String> customerIDColumn;
	@FXML TableColumn<ReservationModel, String> customerPasswordColumn;
	@FXML TableColumn<ReservationModel, String> reservedRoomNumberColumn;
	@FXML TextField customerSearchField;
	
	@FXML AnchorPane roomManagementPane;  //객실을 추가하거나 삭제할 수 있는 예약 관리 화면
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
	
	@FXML AnchorPane managementSelectionPane;  //예약을 조회하거나 객실 관리를 선택하는 화면
	@FXML ToggleGroup managementSelection;
	
	@FXML AnchorPane managerLoginPane;  //관리자 로그인 화면
	@FXML TextField hotelNameField;
	@FXML PasswordField passwordField;
	
	Socket socket;
	String ID;
	List<RoomModel> roomModelList = FXCollections.observableArrayList();  //고객이 이용 가능한 객실 목록
	List<ReservationModel> reservationModelList = FXCollections.observableArrayList();  //해당 호텔을 예약한 예약 목록
	FilteredList<ReservationModel> filteredList = new FilteredList<ReservationModel>((ObservableList<ReservationModel>) reservationModelList);  //예약의 결과를 필터해서 보여주는 목록
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {  //해당 테이블 뷰에 목록을 할당
		reservationTableView.setPlaceholder(new Label("예약 고객 없음"));
		reservationTableView.setItems((ObservableList<ReservationModel>) reservationModelList);
		customerNameColumn.setCellValueFactory(param -> param.getValue().getCustomerNameProperty());
		phoneNumberColumn.setCellValueFactory(param -> param.getValue().getPhoneNumberProperty());
		customerIDColumn.setCellValueFactory(param -> param.getValue().getCustomerIDProperty());
		customerPasswordColumn.setCellValueFactory(param -> param.getValue().getCustomerPasswordProperty());
		reservedRoomNumberColumn.setCellValueFactory(param -> param.getValue().getRoomNumberProperty());
		
		availableRoomTableView.setPlaceholder(new Label("객실 없음"));
		availableRoomTableView.setItems((ObservableList<RoomModel>) roomModelList);
		availableRoomNumberColumn.setCellValueFactory(param -> param.getValue().getRoomNumberProperty());
		availableRoomTypeColumn.setCellValueFactory(param -> param.getValue().getRoomTypeProperty());
		availableRoomOriginalPriceColumn.setCellValueFactory(param -> param.getValue().getOriginalPriceProperty());
		availableRoomReducedPriceColumn.setCellValueFactory(param -> param.getValue().getReducedPriceProperty());
		availableRoomReservationPeriodColumn.setCellValueFactory(param -> param.getValue().getReservationPeriodProperty());
		
		startClient();  //클라이언트 시작
	}
	
	void startClient() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                	socket = new Socket();  //소켓 생성 후 연결
                    socket.connect(new InetSocketAddress("localhost", 7777));
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                byte[] bytes = "Manager".getBytes(StandardCharsets.UTF_8);  //관리자이기 때문에 관리자라는 것을 알리기 위해 Manager를 보냄
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
	
    void receive() {  //요청이 있을 때마다 받아서 처리하는 메소드
        while (true) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                Protocol protocol = (Protocol) objectInputStream.readObject();
                
                if (protocol.getMessage().contentEquals("noAccount")) {  //계정이 없는 경우
                	Platform.runLater(() -> {
                		new Alert(Alert.AlertType.WARNING, "계정이 없습니다.", ButtonType.CLOSE).show();
                	});
                }
                
                if (protocol.getMessage().contentEquals("existAccount")) {  //계정이 있는 경우
                	String password = (String) protocol.getObject();
                	if (password.contentEquals(passwordField.getText())) {
            			Platform.runLater(() -> {
            				Alert alert = new Alert(Alert.AlertType.INFORMATION, "로그인에 성공하셨습니다.", ButtonType.CLOSE);
                			alert.showAndWait();
                			stackPane.getChildren().remove(managerLoginPane);
                			hotelNameField.setText("");
                			passwordField.setText("");
            			});
            		}
                	else {
                		Platform.runLater(() -> {
                			new Alert(Alert.AlertType.ERROR, "비밀번호가 일치하지 않습니다.", ButtonType.CLOSE).show();
                		});
                	}
                }
                
                if (protocol.getMessage().contentEquals("returnRoomList")) {  //고객이 이용 가능한 객실 목록 조회
                	List<Room> roomList = (List<Room>) protocol.getObject();
                	roomModelList.clear();
                	for (int i=0; i<roomList.size(); i++) {
                		roomModelList.add(new RoomModel(roomList.get(i).getHotelName(), roomList.get(i).getRoomNumber(), roomList.get(i).getRoomType(), roomList.get(i).getOriginalPrice(), roomList.get(i).getReducedPrice(), roomList.get(i).getReservationPeriod()));
                	}
                }
                
                if (protocol.getMessage().contentEquals("returnCustomerList")) {  //예약한 고객의 정보를 가져옴
                	List<Customer> list = (List<Customer>) protocol.getObject();
                	reservationModelList.clear();
        			for (int i=0; i<list.size(); i++) {
            			for (int j=0; j<list.get(i).getReservedRoomList().size(); j++) {
            				if (list.get(i).getReservedRoomList().get(j).getHotelName().contentEquals(ID))
            					reservationModelList.add(new ReservationModel(list.get(i).getID(), list.get(i).getPassword(), list.get(i).getName(), list.get(i).getPhoneNumber(), list.get(i).getReservedRoomList().get(j).getRoomNumber()));
            			}
            		}
                }
                
                if (protocol.getMessage().contentEquals("deleteAvailableRoom")) {  //외부에서 전화 등의 수단을 통해 예약을 한 경우
                	Room room = (Room) protocol.getObject();                       //해당 방을 삭제하면 서버에서 삭제하고 고객 클라이언에도 실시간으로
                	for (int i=0; i<roomModelList.size(); i++) {                   //반영하도록 하기 위해 서버에 요청 보냄
                		if (roomModelList.get(i).getHotelName().contentEquals(room.getHotelName())&&roomModelList.get(i).getRoomNumber().contentEquals(room.getRoomNumber()))
                			roomModelList.remove(i);
                	}
                	send("getCustomerList", ID);
                }
                
                if (protocol.getMessage().contentEquals("addReservedRoom")) {  //예약이 취소된 방을 추가
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
    
    <T> void send(String message, T object) {  //요청을 서버에 보내는 메소드
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

    void disconnectServer() {  //서버와의 연결을 끊는 메소드
    	if (socket != null && !socket.isClosed()) {
			 try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		 }
    }
	
	// reservationManagementPane
	@FXML public void moveFromResrvationPaneToSelectionPane() {  //관리를 선택하는 화면으로 돌아가기
		customerSearchField.setText("");
		reservationTableView.setItems((ObservableList<ReservationModel>) reservationModelList);
		stackPane.getChildren().add(roomManagementPane);
		stackPane.getChildren().add(managementSelectionPane);
	}
	@FXML public void searchAction() {  //예약 목록 중 검색어를 포함한 이름을 가진 결과를 보여줌
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
	@FXML public void quitAction2() {  //시스템 종료
		quitAction1();
	}
	
	// roomManagementPane
	@FXML public void addAvailableRoomAction() {  //이용 가능한 객실을 추가하기
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				String roomNumber = availableRoomNumberField.getText();
				String roomType = availableRoomTypeField.getText();
				String originalPrice = availableRoomOriginalPriceField.getText();
				String reducedPrice = availableRoomReducedPriceField.getText();
				String reservationPeriod = availableRoomInDatePicker.getValue()+"~"+availableRoomOutDatePicker.getValue();
				
				Room room = null;
				if (roomType.contentEquals("2인실")) {  //2인실일 경우
					room = new DoubleRoom(ID, roomNumber, originalPrice, reducedPrice, reservationPeriod);
				}
				else {  //4인실일 경우
					room = new QuadRoom(ID, roomNumber, originalPrice, reducedPrice,reservationPeriod);
				}
				send("addAvailableRoom", room);  //이용 가능한 방이 추가되었다는 것을 서버에 알려 저장시킴
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
	@FXML public void deleteReservedRoomAction() {  //이 프로그램을 사용하지 않고 예약 된 객실을 삭제
		RoomModel roomModel = availableRoomTableView.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "객실을 삭제하시겠습니까?", ButtonType.OK, ButtonType.CANCEL);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get()==ButtonType.OK) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					send("deleteReservedRoom", roomModel.getRoomNumber());
					Platform.runLater(() -> {
						new Alert(Alert.AlertType.INFORMATION, "객실이 삭제되었습니다.", ButtonType.CLOSE).show();
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
	@FXML public void quitAction1() {  //시스템 종료
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				send("quit", "");  //서버에 종료되었다는 것을 알림
				disconnectServer();  //서버 연결을 끊음
				System.exit(0);  //시스템 종료
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	
	// managementSelectionPane
	@FXML public void checkAction() {
		if (managementSelection.getSelectedToggle()!=null) {  //선택 관리 화면에서 선택이 되었을 경우
			if (managementSelection.getSelectedToggle().getUserData().toString().contentEquals("roomManagement")) {  //방 관리인 경우
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
			else if (managementSelection.getSelectedToggle().getUserData().toString().contentEquals("reservationManagement")) {  //예약 목록 조회일 경우
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
	@FXML public void loginAction() { //관리자 로그인
		ID = hotelNameField.getText();
		String password = passwordField.getText();
		
		if (ID.equals("")) {
            new Alert(Alert.AlertType.WARNING, "아이디를 입력하세요.", ButtonType.CLOSE).show();
            hotelNameField.requestFocus();
            return;
        }
        if (password.equals("")) {
            new Alert(Alert.AlertType.WARNING, "비밀번호를 입력하세요.", ButtonType.CLOSE).show();
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