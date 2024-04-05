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
        // ���� ���� �� ���� ������ ����� ������, ������ ��, ������ ������ 2���� ������
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
			addManager(new Manager("aȣ��", "a", new Hotel("aȣ��")));
			addManager(new Manager("bȣ��", "b", new Hotel("bȣ��")));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		for (int i=0; i<managerList.size(); i++) {  //�����ڰ� �����ϴ� ȣ���� ȣ�� ��Ͽ� ����
			hotelList.add(managerList.get(i).getHotel());
		}
		
		for (int i=0; i<hotelList.size(); i++) {  //ȣ�ڵ��� ������ �ִ� ���ǵ��� ���� ��Ͽ� ����
			for (int j=0; j<hotelList.get(i).getRoomList().size(); j++) {
				roomList.add(hotelList.get(i).getRoomList().get(j));
			}
		}
		
		try {
			serverSocket = new ServerSocket();  //���� ���� ����
            serverSocket.bind(new InetSocketAddress(7777));
		} catch (Exception e) {
			e.printStackTrace();
			if (!serverSocket.isClosed()) {
                stopServer();
            }
            return;
		}
        
		while (true) {  //���� �����尡 ������ ������ ����ؼ� �޴´�.
        	try {
        		Socket socket = serverSocket.accept();
        		byte[] bytes = new byte[256];
        		BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
                int readByteCount = bufferedInputStream.read(bytes);
                String data = new String(bytes, 0, readByteCount, StandardCharsets.UTF_8);
        		
				if (data.contentEquals("Customer")) {  //���� ���
					customerClientList.add(new customerClient(socket));  //�� Ŭ���̾�Ʈ ��Ͽ� �߰�
				}
				if (data.contentEquals("Manager")) {  //�������� ���
					managerClientList.add(new managerClient(socket));  //������ Ŭ���̾�Ʈ ��Ͽ� �߰�
				}
        	} catch (IOException e) {
        		if (!serverSocket.isClosed())
        			stopServer();
        		break;
        	}
        }
    }

    void stopServer() {  //���� ���� �޼ҵ�
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();  //���� ���� ����
            }
            if (service != null && !service.isShutdown()) {
                service.shutdown();  //������ Ǯ ����
            }
        } catch (IOException e) {
            e.printStackTrace();
        }  //�� ���, ������ ����� �ܺο� ����
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
	
	public void addCustomer(Customer customer) {  //���� �߰�
		customerList.add(customer);
	}
	
	public void addManager(Manager manager) {  //�����ڸ� �߰�
		managerList.add(manager);
	}
	
	public Room findAvailableRoom(String hotelName, String roomNumber) {  //�̿� ������ ���� ã��
		for (int i=0; i<managerList.size(); i++) {
			for (int j=0; j<managerList.get(i).getHotel().getRoomList().size(); j++) {
				if (managerList.get(i).getHotel().getRoomList().get(j).getRoomNumber().contentEquals(roomNumber)) {
					return managerList.get(i).getHotel().getRoomList().get(j);
				}
			}
		}
		return null;
	}
	
	public Room findReservedRoom(String hotelName, String roomNumber) {  //������ ���� ã��
		for (int i=0; i<customerList.size(); i++) {
			for (int j=0; j<customerList.get(i).getReservedRoomList().size(); j++) {
				if (customerList.get(i).getReservedRoomList().get(j).getHotelName().contentEquals(hotelName)&&customerList.get(i).getReservedRoomList().get(j).getRoomNumber().contentEquals(roomNumber))
					return customerList.get(i).getReservedRoomList().get(j);
			}
		}
		return null;
	}
	
	public Customer findCustomer(String ID) {  //�� ã��
		for (int i=0; i<customerList.size(); i++) {
			if (ID.contentEquals(customerList.get(i).getID()))
				return customerList.get(i);
			else
				continue;
		}
		return null;
	}
	
	public Manager findManager(String ID) {  //������ ã��
		for (int i=0; i<managerList.size(); i++) {
			if (ID.contentEquals(managerList.get(i).getID()))
				return managerList.get(i);
			else
				continue;
		}
		return null;
	}

    class customerClient {  //�� Ŭ���̾�Ʈ
        Socket socket;
        Customer customer;
        
        public customerClient(Socket socket) {
            this.socket = socket;
            receive();
        }
        
        private void receive() {  //�� Ŭ���̾�Ʈ������ ��û�� �޾Ƶ��̰� ó���ϴ� �޼ҵ�
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                            Protocol protocol = (Protocol) objectInputStream.readObject();
                            
                            if (protocol.getMessage().contentEquals("checkAccount")) {  //���� ���� Ȯ���� ���
                            	customer = findCustomer((String) protocol.getObject());
                            	if (customer==null) {  //������ ���� ���
                            		send("noAccount", "");  //Ŭ���̾�Ʈ�� ������ ���ٰ� ����
                            	}
                            	else {  //������ �ִ� ���
                            		send("existAccount", customer.getPassword());  //Ŭ���̾�Ʈ�� ������ �ִٰ� ����
                            	}
                            }
                            
                            if (protocol.getMessage().contentEquals("getRoomList")) {  //���� ����� ��û�� ���
                            	send("returnRoomList", roomList);  //���� ����� ����
                            }
                            
                            if (protocol.getMessage().contentEquals("quit")) {  //�ý��� ���Ḧ �˸� ���
                            	throw new IOException();  //���ܸ� �߻����� �ش� Ŭ���̾�Ʈ�� Ŭ���̾�Ʈ ��Ͽ��� �����ϰ� ���� ����
                            }
                            
                            if (protocol.getMessage().contentEquals("getReservedRoomList")) {  //����� ���� ����� ���ϴ� ���
                        		send("returnReservedRoomList", customer.getReservedRoomList());  //�ش� ���� ������ �� ��� ����
                        	}
                            
                            if (protocol.getMessage().contentEquals("addReservation")) {  //�ش� ���� ���� ������ ���
                            	synchronized(protocol) {  //���� ������ ���� ���� synchronized ��� �̿�
                            		Room wantToReserveRoom = (Room) protocol.getObject();
                            		Room room = findAvailableRoom(wantToReserveRoom.getHotelName(), wantToReserveRoom.getRoomNumber());
                            		customer.addReservation(room);  //���� ������ �� ��Ͽ� �� �߰�
                            		Manager manager = findManager(room.getHotelName());
                            		manager.deleteRoom(room);  //�����ڰ� �����ϴ� ȣ�ڿ��� �ش� ���� ����
                            		roomList.remove(room);  //���� ��Ͽ��� �ش� ���� ����
                            		for (managerClient client : managerClientList) {  //�ش��ϴ� ȣ���� �����ڿ��� �ش� ���� ����Ǿ��ٴ� ���� �˸�
                            			if (client.manager.getHotel().getHotelName().contentEquals(room.getHotelName())) {
                            				client.send("deleteAvailableRoom", room);
                            			}
                            		}
                            		for (customerClient client : customerClientList) {  //�� Ŭ���̾�Ʈ���� �ش� ���� ����Ǿ��ٴ� ���� �˸�
                            			client.send("deleteAvailableRoom", room);
                            		}
                            	}
                            }
                            
                            if (protocol.getMessage().contentEquals("cancelReservation")) {  //������ ����� ���
                            	Room wantToCancelRoom = (Room) protocol.getObject();
                            	Room room = findReservedRoom(wantToCancelRoom.getHotelName(), wantToCancelRoom.getRoomNumber());
                            	customer.cancelReservation(room);  //�ش� ���� ������ �� ��Ͽ��� �ش� �� ����
                            	Manager manager = findManager(room.getHotelName());
                            	manager.addRoom(room);  //�ش� ȣ�ڿ� ���� �߰�
                            	roomList.add(room);  //���� ��Ͽ� �� �߰�
                            	for (managerClient client : managerClientList) {
                        			if (client.manager.getHotel().getHotelName().contentEquals(room.getHotelName())) {
                        				client.send("addReservedRoom", room);  //�ش� �����ڿ��� ���� ��ҵǾ��ٴ� ���� �˸�
                        			}
                        		}
                            	for (customerClient client : customerClientList) {
                        			client.send("addReservedRoomToAvailableRoomList", room);  //���鿡�� ������ ��ҵǾ��ٴ� ���� �˸�
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
        
        private <T> void send(String message, T object) {  //��û�� ������ �޼ҵ�
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

        private void disconnectCustomerClient() {  //�� Ŭ���̾�Ʈ ����
            try {
                customerClientList.remove(customerClient.this);
                socket.close();
                if (customerClientList.size()==0&&managerClientList.size()==0) {  //������ ��� ������ ���
                	stopServer();  //���� ����
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    class managerClient {  //������ Ŭ���̾�Ʈ
        Socket socket;
        Manager manager;

        public managerClient(Socket socket) {
            this.socket = socket;
            receive();
        }
        
        private void receive() {  //������ Ŭ���̾�Ʈ ��û�� �޴� �޼ҵ�
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                        	ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                            Protocol protocol = (Protocol) objectInputStream.readObject();
                            
                            if (protocol.getMessage().contentEquals("checkAccount")) {  //���� Ȯ���� ���
                            	manager = findManager((String) protocol.getObject());
                            	if (manager==null) {  //������ ���� ���
                            		send("noAccount", "");  //������ ���ٰ� �˸�
                            	}
                            	else {  //������ �ִ� ���
                            		send("existAccount", manager.getPassword());  //������ �����Ѵٰ� �˸�
                            	}
                            }
                            
                            if (protocol.getMessage().contentEquals("getRoomList")) {  //���� ����� ��û�� ���
                            	String hotelName = (String) protocol.getObject();
                            	List<Room> list = new Vector<Room>();
                            	for (int i=0; i<roomList.size(); i++) {
                            		if (roomList.get(i).getHotelName().contentEquals(hotelName))
                            			list.add(roomList.get(i));
                            	}
                            	send("returnRoomList", list);  //�ش� ȣ���� ���Ǹ� ����
                            }
                            
                            if (protocol.getMessage().contentEquals("getCustomerList")) {
                            	send("returnCustomerList", customerList);  //�� ��� ����
                            }
                            
                            if (protocol.getMessage().contentEquals("quit")) {
                            	throw new IOException();  //�ý��� ���Ḧ ���ϴ� ��� ���ܸ� �߻����� ������ ����
                            }
                            
                            if (protocol.getMessage().contentEquals("deleteReservedRoom")) {  //������ �� ��� ���� ������ �濡 �ش��ϴ� ���� ã�� ����
                            	String roomNumber = (String) protocol.getObject();
                        		Room room = findAvailableRoom(manager.getID(), roomNumber);
                        		manager.deleteRoom(room);
                        		roomList.remove(room);
                        		for (customerClient client : customerClientList) {
                        			client.send("deleteReservedRoom", room);  //�� Ŭ���̾�Ʈ ���̺� �信 ��������� �˷� �ǽð����� �ش� ���� �����ϱ� ���� �߽�
                        		}
                            }
                            
                            if (protocol.getMessage().contentEquals("addAvailableRoom")) {  //�����ڰ� �̿� ������ ������ �߰��� ���
                            	Room room = (Room) protocol.getObject();
                            	manager.getHotel().getRoomList().add(room);  //�ش� ȣ�ڿ� ���� �߰�
                            	roomList.add(room);  //���� ��Ͽ� �߰�
                            	for (customerClient client : customerClientList) {
                        			client.send("addAvailableRoom", room);  //���鿡�� �̿� ������ ������ �߰��Ǿ��ٴ� ���� �˸�
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
        
        private <T> void send(String message, T object) {  //�������� Ŭ���̾�Ʈ���� ��û�� ó���ϰ� ����� ������ �޼ҵ�
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

        private void disconnectManagerClient() {  //������ Ŭ���̾�Ʈ�� �����ϴ� �޼ҵ�
            try {
                managerClientList.remove(managerClient.this);
                socket.close();
                if (customerClientList.size()==0&&managerClientList.size()==0) {  //������ ��� ������ ��� ���� ����
                	stopServer();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}