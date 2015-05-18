package indie.pfe.talkin;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

/**
 * Created by Mustapha Essouri on 01/05/2015.
 */
public class ClientManager {
    public static final int BROADCAST_PORT = 50001;
    private static final int BROADCAST_INTERVAL = 10000;
    private static final int BROADCAST_BIF_SIZE = 1024;
    private boolean BROADCAST = true;
    private boolean LISTEN = true;
    private HashMap<String, InetAddress> contacts;
    private InetAddress broadcastIP;

    public ClientManager(String name, InetAddress broadcastIP) {
        contacts = new HashMap<String, InetAddress>();
        this.broadcastIP = broadcastIP;
        listen();
        broadcastName(name, broadcastIP);
    }

    public HashMap<String, InetAddress> getContacts() {
        return contacts;
    }

    public void addContact(String name, InetAddress address) {
        if (!contacts.containsKey(name)) {
            contacts.put(name, address);
            return;
        }
        return;
    }

    public void removeContact(String name) {
        if (contacts.containsKey(name)) {
            contacts.remove(name);
            return;
        }
        return;
    }

    public void bye(final String name) {
        Thread bye = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String notification = "BYE: " + name;
                    byte[] message = notification.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(message, message.length, broadcastIP, BROADCAST_PORT);
                    socket.send(packet);
                    socket.disconnect();
                    socket.close();
                    return;
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        bye.start();
    }

    public void broadcastName(final String name, final InetAddress broadcastIP) {
        Thread broadcast = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String request = "ADD:" + name;
                    byte[] message = request.getBytes();
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(message, message.length, broadcastIP, BROADCAST_PORT);
                    while (BROADCAST) {
                        socket.send(packet);
                        Thread.sleep(BROADCAST_INTERVAL);
                    }
                    socket.disconnect();
                    socket.close();
                    return;
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        broadcast.start();
    }

    public void stopBroadcasting() {
        BROADCAST = false;
    }

    public void listen() {
        Thread listen = new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket(BROADCAST_PORT);
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                }
                byte[] buffer = new byte[BROADCAST_BIF_SIZE];
                while (LISTEN) {
                    listen(socket, buffer);
                }
                socket.disconnect();
                socket.close();
                return;
            }

            public void listen(DatagramSocket socket, byte[] buffer) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, BROADCAST_BIF_SIZE);
                    socket.setSoTimeout(15000);
                    socket.receive(packet);
                    String data = new String(buffer, 0, packet.getLength());
                    String action = data.substring(0, 4);
                    if (action.equals("ADD:")) {
                        addContact(data.substring(4, data.length()), packet.getAddress());
                    } else if (action.equals("BYE:")) {
                        removeContact(data.substring(4, data.length()));
                    } else {
                        return;
                    }
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    if (LISTEN)
                        listen(socket, buffer);
                    return;
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        });
        listen.start();
    }

    public void stopListening() {
        LISTEN = false;
    }
}
