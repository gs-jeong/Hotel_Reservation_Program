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
	
	@FXML AnchorPane checkReservationPane;  //예약 조회 화면
	@FXML TableView<RoomModel> reservedRoomTableView;  //예약한 방을 표시하는 테이블 뷰
	@FXML TableColumn<RoomModel, String> reservedHotelNameColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomNumberColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomTypeColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomOriginalPriceColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomReducedPriceColumn;
	@FXML TableColumn<RoomModel, String> reservedRoomReservationPeriodColumn;
	
	@FXML AnchorPane reservationPane;  //방을 예야하는 화면
	@FXML TableView<RoomModel> availableRoomTableView;  //예약 가능한 방을 표시하는 테이블 뷰
	@FXML TableColumn<RoomModel, String> availableHotelNameColumn;
	@FXML TableColumn<RoomModel, String> availableRoomNumberColumn;
	@FXML TableColumn<RoomModel, String> availableRoomTypeColumn;
	@FXML TableColumn<RoomModel, String> availableRoomOriginalPriceColumn;
	@FXML TableColumn<RoomModel, String> availableRoomReducedPriceColumn;
	@FXML TableColumn<RoomModel, String> availableRoomReservationPeriodColumn;
	@FXML TextField searchField;
	
	@FXML AnchorPane loginPane;  //고객 로그인 화면
	@FXML TextField IDField;
	@FXML PasswordField passwordField;
	
	Socket socket;
	String ID;
	List<RoomModel> roomModelList = FXCollections.observableArrayList();  //예약 가능한 방 리스트
	FilteredList<RoomModel> filteredList = new FilteredList<RoomModel>((ObservableList<RoomModel>) roomModelList);  //검색 시 예약 가능한 방의 결과를 필터해서 보여주는 리스트
	List<RoomModel> reservedRoomModelList = FXCollections.observableArrayList();  //예약한 방 리스트
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {  //리스트들을 해당하는 테이블 부에 할당
		availableRoomTableView.setPlaceholder(new Label("객실 없음"));
		availableRoomTableView.setItems((ObservableList<RoomModel>) roomModelList);
		availableHotelNameColumn.setCellValueFactory(param -> param.getValue().getHotelNameProperty());
		availableRoomNumberColumn.setCellValueFactory(param -> param.getValue().getRoomNumberProperty());
		availableRoomTypeColumn.setCellValueFactory(param -> param.getValue().getRoomTypeProperty());
		availableRoomOriginalPriceColumn.setCellValueFactory(param -> param.getValue().getOriginalPriceProperty());
		availableRoomReducedPriceColumn.setCellValueFactory(param -> param.getValue().getReducedPriceProperty());
		availableRoomReservationPeriodColumn.setCellValueFactory(param -> param.getValue().getReservationPeriodProperty());
		
		reservedRoomTableView.setPlaceholder(new Label("예약 목록 없음"));
		reservedRoomTableView.setItems((ObservableList<RoomModel>) reservedRoomModelList);
		reservedHotelNameColumn.setCellValueFactory(param -> param.getValue().getHotelNameProperty());
		reservedRoomNumberColumn.setCellValueFactory(param -> param.getValue().getRoomNumberProperty());
		reservedRoomTypeColumn.setCellValueFactory(param -> param.getValue().getRoomTypeProperty());
		reservedRoomOriginalPriceColumn.setCellValueFactory(param -> param.getValue().getOriginalPriceProperty());
		reservedRoomReducedPriceColumn.setCellValueFactory(param -> param.getValue().getReducedPriceProperty());
		reservedRoomReservationPeriodColumn.setCellValueFactory(param -> param.getValue().getReservationPeriodProperty());
		
		startClient();  //클라이언트 시작
	}
	
	void startClient() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                	socket = new Socket();  //소켓을 만들고 연결
                    socket.connect(new InetSocketAddress("localhost", 7777));
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                byte[] bytes = "Customer".getBytes(StandardCharsets.UTF_8);  //고객 클라이언트이기 때문에 고객이라는 메세지를 보내 서버에서 고객 클라이언트에 소켓을 담도록 만듦
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
                                bufferedOutputStream.write(bytes);  //Customer를 서버로 보냄
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
                receive();  //요청을 받아들이는 메소드
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
	
    void receive() {  //요청이 있을 때마다 계속 받아들이는 메소드
        while (true) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                Protocol protocol = (Protocol) objectInputStream.readObject();  //protocol이 가진 메세지에 따라 다른 일을 수행
                
                if (protocol.getMessage().contentEquals("noAccount")) {  //로그인시 계정이 없는 경우
                	Platform.runLater(() -> {
                		new Alert(Alert.AlertType.WARNING, "계정이 없습니다.", ButtonType.CLOSE).show();
                	});
                }
                
                if (protocol.getMessage().contentEquals("existAccount")) {  //로그인시 계정이 있는 경우
                	String password = (String) protocol.getObject();
                	if (password.contentEquals(passwordField.getText())) {  //비밀번호가 일치한 경우
            			Platform.runLater(() -> {
            				Alert alert = new Alert(Alert.AlertType.INFORMATION, "로그인에 성공하셨습니다.", ButtonType.CLOSE);
                			alert.showAndWait();
                			stackPane.getChildren().remove(loginPane);
                			IDField.setText("");
                			passwordField.setText("");
                			send("getRoomList", "");
            			});
            		}
                	else {
                		Platform.runLater(() -> {  //비밀번호가 틀린 경우
                			new Alert(Alert.AlertType.ERROR, "비밀번호가 일치하지 않습니다.", ButtonType.CLOSE).show();
                		});
                	}
                }
                
                if (protocol.getMessage().contentEquals("returnRoomList")) {  //이용 가능한 방 목록을 가져오기
                	List<Room> roomList = (List<Room>) protocol.getObject();
                	for (int i=0; i<roomList.size(); i++) {
                		roomModelList.add(new RoomModel(roomList.get(i).getHotelName(), roomList.get(i).getRoomNumber(), roomList.get(i).getRoomType(), roomList.get(i).getOriginalPrice(), roomList.get(i).getReducedPrice(), roomList.get(i).getReservationPeriod()));
                	}
                }
                
                if (protocol.getMessage().contentEquals("returnReservedRoomList")) {  //예약한 방 목록을 가져오기
                	List<Room> reservedRoomList = (List<Room>) protocol.getObject();
                	reservedRoomModelList.clear();
        			for (int i=0; i<reservedRoomList.size(); i++) {
            			reservedRoomModelList.add(new RoomModel(reservedRoomList.get(i).getHotelName(), reservedRoomList.get(i).getRoomNumber(), reservedRoomList.get(i).getRoomType(), reservedRoomList.get(i).getOriginalPrice(), reservedRoomList.get(i).getReducedPrice(), reservedRoomList.get(i).getReservationPeriod()));
            		}
                }
                
                if (protocol.getMessage().contentEquals("deleteAvailableRoom")) {  //예약을 했을 경우 이용가능한 방 목록에서 해당 방을 제외
                	Room room = (Room) protocol.getObject();
                	for (int i=0; i<roomModelList.size(); i++) {
                		if (roomModelList.get(i).getHotelName().contentEquals(room.getHotelName())&&roomModelList.get(i).getRoomNumber().contentEquals(room.getRoomNumber()))
                			roomModelList.remove(i);
                	}
                }
                
                if (protocol.getMessage().contentEquals("addReservedRoomToAvailableRoomList")) {  //예약 취소를 할 경우 해당 방을 이용가능한 방 목록에 추가
                	Room room = (Room) protocol.getObject();
                	for (int i=0; i<reservedRoomModelList.size(); i++) {
                		if (reservedRoomModelList.get(i).getHotelName().contentEquals(room.getHotelName())&&reservedRoomModelList.get(i).getRoomNumber().contentEquals(room.getRoomNumber())) {
                			reservedRoomModelList.remove(i);
                		}
                	}
                	roomModelList.add(new RoomModel(room.getHotelName(), room.getRoomNumber(), room.getRoomType(), room.getOriginalPrice(), room.getReducedPrice(), room.getReservationPeriod()));
                }
                
                if (protocol.getMessage().contentEquals("deleteReservedRoom")) {  //이 프로그램을 사용하지 않고 외부 수단을 통해 방을 예약한 경우
                	Room room = (Room) protocol.getObject();                      //관리자가 해당 방을 삭제하면 고객 클라이언트에서도 삭제
                	for (int i=0; i<roomModelList.size(); i++) {
                		if (roomModelList.get(i).getHotelName().contentEquals(room.getHotelName())&&roomModelList.get(i).getRoomNumber().contentEquals(room.getRoomNumber()))
                			roomModelList.remove(i);
                	}
                }
                
                if (protocol.getMessage().contentEquals("addAvailableRoom")) {  //관리자가 이용 가능한 방을 추가하면
                	Room room = (Room) protocol.getObject();                    //고객 클라이언트에 표시
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
    
    <T> void send(String message, T object) {  //서버에 어떤 요청인지의 메세지와 그에 필요한 객체를 만들어 보내는 메소드
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
	
	// checkReservationPane
	@FXML public void cancelReservationAction() {  //예약 취소 메소드
		RoomModel roomModel = reservedRoomTableView.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "예약을 취소하시겠습니까?", ButtonType.OK, ButtonType.CANCEL);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get()==ButtonType.OK) {  //예약 취소를 OK버튼으로 확인했을 때
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Room room = null;
					if (roomModel.getRoomType().contentEquals("2인실")) {  //2인실일 경우 2인실 객체를 생성
						room = new DoubleRoom(roomModel.getHotelName(), roomModel.getRoomNumber(), roomModel.getOriginalPrice(), roomModel.getReducedPrice(), roomModel.getReservationPeriod());
					}
					else {  //4인실일 경우 4인실 객체 생성
						room = new QuadRoom(roomModel.getHotelName(), roomModel.getRoomNumber(), roomModel.getOriginalPrice(), roomModel.getReducedPrice(), roomModel.getReservationPeriod());
					}
					send("cancelReservation", room);  //서버에 해당 방이 예약이 취소되었다는 것을 알림
				}
			};
			Thread t = new Thread(runnable);
			t.start();
		}
	}
	@FXML public void moveToPreviousPageAction() {  //이전 화면으로 돌아가는 메소드
		stackPane.getChildren().add(reservationPane);
	}
	@FXML public void quitAction2() {  //고객 클라이언트 종료 메소드
		quitAction1();
	}
	
	// reservationPane
	@FXML public void reserveAvailableRoomAction() {  //이용 가능한 방 예약 메소드
		RoomModel roomModel = availableRoomTableView.getSelectionModel().getSelectedItem();
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "예약하시겠습니까?", ButtonType.OK, ButtonType.CANCEL);
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get()==ButtonType.OK) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					Room room = null;
					if (roomModel.getRoomType().contentEquals("2인실")) {  //해당하는 객체 생성
						room = new DoubleRoom(roomModel.getHotelName(), roomModel.getRoomNumber(), roomModel.getOriginalPrice(), roomModel.getReducedPrice(), roomModel.getReservationPeriod());
					}
					else {
						room = new QuadRoom(roomModel.getHotelName(), roomModel.getRoomNumber(), roomModel.getOriginalPrice(), roomModel.getReducedPrice(), roomModel.getReservationPeriod());
					}
					send("addReservation", room);  //해당 방이 예약되었다는 것을 알림
				}
			};
			Thread t = new Thread(runnable);
			t.start();
		}
	}
	@FXML public void checkReservationAction() {  //예약 확인 메소드
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				send("getReservedRoomList", "");  //예약된 방의 목록을 서버에서 가져오기 위해 서버에 요청
				Platform.runLater(() -> {
					stackPane.getChildren().remove(reservationPane);  //예약확인 화면으로 이동
				});
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	@FXML public void searchAction() {  //이용 가능한 방 중 호텔이름이 검색어의 내용을 포함하고 있는 호텔의 객식들을 보여줌
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
	@FXML public void quitAction1() {  //고객 클라이언트 종료 메소드
		Runnable runnable = new Runnable() {	
			@Override
			public void run() {
				send("quit", "");  //서버에 종료하겠다는 요청을 보냄
				disconnectServer();  //서버 연결을 끊음
				System.exit(0);  //시스템 종료
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
	
	// loginPane
	@FXML public void loginAction() {  //고객 로그인
		ID = IDField.getText();
        String password = passwordField.getText();
        
		if (ID.equals("")) {
            new Alert(Alert.AlertType.WARNING, "아이디를 입력하세요.", ButtonType.CLOSE).show();
            IDField.requestFocus();
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
				send("checkAccount", ID);  //계정의 유무를 확인
			}
		};
		Thread t = new Thread(runnable);
		t.start();
	}
}