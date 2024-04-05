package HotelReservationServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Class.Customer;
import Class.Hotel;
import Class.Manager;
import Class.Room;
import Protocol.Protocol;

public class HotelReservationServer {
	public static void main(String[] args) {
		new HotelReservationServer();
    }
	
	ExecutorService service;
    
    private List<customerClient> customerClientList = new Vector<>();
    private List<managerClient> managerClientList = new Vector<>();
    private ServerSocket serverSocket;
    private List<Customer> customerList = null;
    private List<Manager> managerList = null;
    private List<Hotel> hotelList = new Vector<Hotel>();
    private List<Room> roomList = new Vector<Room>();

    public HotelReservationServer() {
        service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
        // 서버 시작 시 고객과 관리자 목록을 가져옴, 없으면 고객, 관리자 계정을 2개씩 만들음
		try {
			ObjectInputStream customerInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream("./src/File/customerList.obj")));
			customerList = (Vector<Customer>) customerInputStream.readObject();
			customerInputStream.close();
		} catch (FileNotFoundException e) {
			customerList = new Vector<Customer>();
			addCustomer(new Customer("a", "a", "CNU1", "010-0000-0000"));
			addCustomer(new Customer("b", "b", "CNU2", "010-0000-0001"));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			ObjectInputStream managerInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream("./src/File/managerList.obj")));
        	managerList = (Vector<Manager>) managerInputStream.readObject();
        	managerInputStream.close();
		} catch (FileNotFoundException e) {
			managerList = new Vector<Manager>();
			addManager(new Manager("a호텔", "a", new Hotel("a호텔")));
			addManager(new Manager("b호텔", "b", new Hotel("b호텔")));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		for (int i=0; i<managerList.size(); i++) {  //관리자가 관리하는 호텔을 호텔 목록에 저장
			hotelList.add(managerList.get(i).getHotel());
		}
		
		for (int i=0; i<hotelList.size(); i++) {  //호텔들이 가지고 있는 객실들을 객실 목록에 저장
			for (int j=0; j<hotelList.get(i).getRoomList().size(); j++) {
				roomList.add(hotelList.get(i).getRoomList().get(j));
			}
		}
		
		try {
			serverSocket = new ServerSocket();  //서버 소켓 생성
            serverSocket.bind(new InetSocketAddress(7777));
		} catch (Exception e) {
			e.printStackTrace();
			if (!serverSocket.isClosed()) {
                stopServer();
            }
            return;
		}
        
		while (true) {  //메인 스레드가 소켓의 연결을 계속해서 받는다.
        	try {
        		Socket socket = serverSocket.accept();
        		byte[] bytes = new byte[256];
        		BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                int readByteCount = bufferedInputStream.read(bytes);
                String data = new String(bytes, 0, readByteCount, StandardCharsets.UTF_8);
        		
				if (data.contentEquals("Customer")) {  //고객인 경우
					customerClientList.add(new customerClient(socket));  //고객 클라이언트 목록에 추가
				}
				if (data.contentEquals("Manager")) {  //관리자인 경우
					managerClientList.add(new managerClient(socket));  //관리자 클라이언트 목록에 추가
				}
        	} catch (IOException e) {
        		if (!serverSocket.isClosed())
        			stopServer();
        		break;
        	}
        }
    }

    void stopServer() {  //서버 종료 메소드
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();  //서버 소켓 종료
            }
            if (service != null && !service.isShutdown()) {
                service.shutdown();  //스레드 풀 종료
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  //고객 목록, 관리자 목록을 외부에 저장
        try {
        	ObjectOutputStream customerOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("./src/File/customerList.obj")));
            customerOutputStream.writeObject(customerList);
            customerOutputStream.flush();
            customerOutputStream.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        try {
        	ObjectOutputStream managerOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("./src/File/managerList.obj")));
        	managerOutputStream.writeObject(managerList);
        	managerOutputStream.flush();
        	managerOutputStream.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
	
	public void addCustomer(Customer customer) {  //고객을 추가
		customerList.add(customer);
	}
	
	public void addManager(Manager manager) {  //관리자를 추가
		managerList.add(manager);
	}
	
	public Room findAvailableRoom(String hotelName, String roomNumber) {  //이용 가능한 방을 찾기
		for (int i=0; i<managerList.size(); i++) {
			for (int j=0; j<managerList.get(i).getHotel().getRoomList().size(); j++) {
				if (managerList.get(i).getHotel().getRoomList().get(j).getRoomNumber().contentEquals(roomNumber)) {
					return managerList.get(i).getHotel().getRoomList().get(j);
				}
			}
		}
		return null;
	}
	
	public Room findReservedRoom(String hotelName, String roomNumber) {  //예약한 방을 찾기
		for (int i=0; i<customerList.size(); i++) {
			for (int j=0; j<customerList.get(i).getReservedRoomList().size(); j++) {
				if (customerList.get(i).getReservedRoomList().get(j).getHotelName().contentEquals(hotelName)&&customerList.get(i).getReservedRoomList().get(j).getRoomNumber().contentEquals(roomNumber))
					return customerList.get(i).getReservedRoomList().get(j);
			}
		}
		return null;
	}
	
	public Customer findCustomer(String ID) {  //고객 찾기
		for (int i=0; i<customerList.size(); i++) {
			if (ID.contentEquals(customerList.get(i).getID()))
				return customerList.get(i);
			else
				continue;
		}
		return null;
	}
	
	public Manager findManager(String ID) {  //관리자 찾기
		for (int i=0; i<managerList.size(); i++) {
			if (ID.contentEquals(managerList.get(i).getID()))
				return managerList.get(i);
			else
				continue;
		}
		return null;
	}

    class customerClient {  //고객 클라이언트
        Socket socket;
        Customer customer;
        
        public customerClient(Socket socket) {
            this.socket = socket;
            receive();
        }
        
        private void receive() {  //고객 클라이언트에서의 요청을 받아들이고 처리하는 메소드
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                            Protocol protocol = (Protocol) objectInputStream.readObject();
                            
                            if (protocol.getMessage().contentEquals("checkAccount")) {  //계정 유무 확인일 경우
                            	customer = findCustomer((String) protocol.getObject());
                            	if (customer==null) {  //계정이 없는 경우
                            		send("noAccount", "");  //클라이언트에 계정이 없다고 전달
                            	}
                            	else {  //계정이 있는 경우
                            		send("existAccount", customer.getPassword());  //클라이언트에 계정이 있다고 전달
                            	}
                            }
                            
                            if (protocol.getMessage().contentEquals("getRoomList")) {  //객실 목록을 요청한 경우
                            	send("returnRoomList", roomList);  //객실 목록을 보냄
                            }
                            
                            if (protocol.getMessage().contentEquals("quit")) {  //시스템 종료를 알린 경우
                            	throw new IOException();  //예외를 발생시켜 해당 클라이언트를 클라이언트 목록에서 제외하고 연결 끊음
                            }
                            
                            if (protocol.getMessage().contentEquals("getReservedRoomList")) {  //예약된 객실 목록을 원하는 경우
                        		send("returnReservedRoomList", customer.getReservedRoomList());  //해당 고객이 예약한 방 목록 전송
                        	}
                            
                            if (protocol.getMessage().contentEquals("addReservation")) {  //해당 고객이 방을 예약한 경우
                            	synchronized(protocol) {  //동시 예약을 막기 위해 synchronized 블록 이용
                            		Room wantToReserveRoom = (Room) protocol.getObject();
                            		Room room = findAvailableRoom(wantToReserveRoom.getHotelName(), wantToReserveRoom.getRoomNumber());
                            		customer.addReservation(room);  //고객이 예약한 방 목록에 방 추가
                            		Manager manager = findManager(room.getHotelName());
                            		manager.deleteRoom(room);  //관리자가 관리하는 호텔에서 해당 객실 삭제
                            		roomList.remove(room);  //객실 목록에서 해당 객실 삭제
                            		for (managerClient client : managerClientList) {  //해당하는 호텔의 관리자에게 해당 방이 예약되었다는 것을 알림
                            			if (client.manager.getHotel().getHotelName().contentEquals(room.getHotelName())) {
                            				client.send("deleteAvailableRoom", room);
                            			}
                            		}
                            		for (customerClient client : customerClientList) {  //고객 클라이언트에게 해당 방이 예약되었다는 것을 알림
                            			client.send("deleteAvailableRoom", room);
                            		}
                            	}
                            }
                            
                            if (protocol.getMessage().contentEquals("cancelReservation")) {  //예약을 취소한 경우
                            	Room wantToCancelRoom = (Room) protocol.getObject();
                            	Room room = findReservedRoom(wantToCancelRoom.getHotelName(), wantToCancelRoom.getRoomNumber());
                            	customer.cancelReservation(room);  //해당 고객의 예약한 방 목록에서 해당 방 삭제
                            	Manager manager = findManager(room.getHotelName());
                            	manager.addRoom(room);  //해당 호텔에 방을 추가
                            	roomList.add(room);  //객실 목록에 방 추가
                            	for (managerClient client : managerClientList) {
                        			if (client.manager.getHotel().getHotelName().contentEquals(room.getHotelName())) {
                        				client.send("addReservedRoom", room);  //해당 관리자에게 방이 취소되었다는 것을 알림
                        			}
                        		}
                            	for (customerClient client : customerClientList) {
                        			client.send("addReservedRoomToAvailableRoomList", room);  //고객들에게 예약이 취소되었다는 것을 알림
                        		}
                            }
                        }
                    } catch (IOException e) {
                        disconnectCustomerClient();
                    } catch (ClassNotFoundException e) {
                    	disconnectCustomerClient();
                    }
                }
            };
            service.submit(runnable);
        }
        
        private <T> void send(String message, T object) {  //요청을 보내는 메소드
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                    	ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        objectOutputStream.writeObject(new Protocol<T>(message, object));
                        objectOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        disconnectCustomerClient();
                    }
                }
            };
            service.submit(runnable);
        }

        private void disconnectCustomerClient() {  //고객 클라이언트 종료
            try {
                customerClientList.remove(customerClient.this);
                socket.close();
                if (customerClientList.size()==0&&managerClientList.size()==0) {  //연결이 모두 끊어진 경우
                	stopServer();  //서버 정지
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    class managerClient {  //관리자 클라이언트
        Socket socket;
        Manager manager;

        public managerClient(Socket socket) {
            this.socket = socket;
            receive();
        }
        
        private void receive() {  //관리자 클라이언트 요청을 받는 메소드
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                        	ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                            Protocol protocol = (Protocol) objectInputStream.readObject();
                            
                            if (protocol.getMessage().contentEquals("checkAccount")) {  //계정 확인인 경우
                            	manager = findManager((String) protocol.getObject());
                            	if (manager==null) {  //계정이 없는 경우
                            		send("noAccount", "");  //계정이 없다고 알림
                            	}
                            	else {  //계정이 있는 경우
                            		send("existAccount", manager.getPassword());  //계정이 존재한다고 알림
                            	}
                            }
                            
                            if (protocol.getMessage().contentEquals("getRoomList")) {  //객실 목록을 요청한 경우
                            	String hotelName = (String) protocol.getObject();
                            	List<Room> list = new Vector<Room>();
                            	for (int i=0; i<roomList.size(); i++) {
                            		if (roomList.get(i).getHotelName().contentEquals(hotelName))
                            			list.add(roomList.get(i));
                            	}
                            	send("returnRoomList", list);  //해당 호텔의 객실만 전송
                            }
                            
                            if (protocol.getMessage().contentEquals("getCustomerList")) {
                            	send("returnCustomerList", customerList);  //고객 목록 전송
                            }
                            
                            if (protocol.getMessage().contentEquals("quit")) {
                            	throw new IOException();  //시스템 종료를 원하는 경우 예외를 발생시켜 연결을 끊음
                            }
                            
                            if (protocol.getMessage().contentEquals("deleteReservedRoom")) {  //예약이 된 경우 고객이 예약한 방에 해당하는 방을 찾아 삭제
                            	String roomNumber = (String) protocol.getObject();
                        		Room room = findAvailableRoom(manager.getID(), roomNumber);
                        		manager.deleteRoom(room);
                        		roomList.remove(room);
                        		for (customerClient client : customerClientList) {
                        			client.send("deleteReservedRoom", room);  //고객 클라이언트 테이블 뷰에 변경사항을 알려 실시간으로 해당 방을 삭제하기 위해 발신
                        		}
                            }
                            
                            if (protocol.getMessage().contentEquals("addAvailableRoom")) {  //관리자가 이용 가능한 객실을 추가한 경우
                            	Room room = (Room) protocol.getObject();
                            	manager.getHotel().getRoomList().add(room);  //해당 호텔에 객실 추가
                            	roomList.add(room);  //객실 목록에 추가
                            	for (customerClient client : customerClientList) {
                        			client.send("addAvailableRoom", room);  //고객들에게 이용 가능한 객실이 추가되었다는 것을 알림
                        		}
                            }
                        }
                    } catch (IOException e) {
                        disconnectManagerClient();
                    } catch (ClassNotFoundException e) {
                    	disconnectManagerClient();
                    }
                }
            };
            service.submit(runnable);
        }
        
        private <T> void send(String message, T object) {  //서버에서 클라이언트에게 요청을 처리하고 결과를 보내는 메소드
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                    	ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        objectOutputStream.writeObject(new Protocol<T>(message, object));
                        objectOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        disconnectManagerClient();
                    }
                }
            };
            service.submit(runnable);
        }

        private void disconnectManagerClient() {  //관리자 클라이언트를 종료하는 메소드
            try {
                managerClientList.remove(managerClient.this);
                socket.close();
                if (customerClientList.size()==0&&managerClientList.size()==0) {  //연결이 모두 끊어진 경우 서버 종료
                	stopServer();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}