package uk.co.caprica.vlcjplayer.service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lombok.AllArgsConstructor;
import lombok.val;

public class Broadcaster {
	Timer timer = new Timer();
	static final int TARGET_PORT = 8877;

	public Broadcaster() {
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				sendMessage("DJ", TARGET_PORT);
			}
		}, 0, 20000);
	}

	public static void sendMessage(String msg, int port) {
		try {
			for (Tuple dresses : getNonLoopbackAddress()) {

				// Open a new DatagramSocket, which will be used to send the
				// data.
				try (MulticastSocket serverSocket = new MulticastSocket()) {
					serverSocket.setBroadcast(true);
					// Create a packet that will contain the data
					// (in the form of bytes) and send it.
					DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, dresses.all,
							port);
					serverSocket.send(msgPacket);

					System.out.println("Server sent packet with msg: " + msg);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<Tuple> getNonLoopbackAddress() throws SocketException, UnknownHostException {
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		val list = new ArrayList<Tuple>();
		while (en.hasMoreElements()) {
			NetworkInterface i = (NetworkInterface) en.nextElement();
			for (Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements();) {
				InetAddress addr = (InetAddress) en2.nextElement();
				if (!addr.isLoopbackAddress()) {
					if (addr instanceof Inet4Address) {
						val stringDress = addr.toString().split("/")[1];
						list.add(new Tuple(addr, InetAddress
								.getByName(stringDress.substring(0, stringDress.lastIndexOf('.') + 1) + "255")));
					}

				}
			}
		}
		return list;
	}

	@AllArgsConstructor
	static class Tuple {
		public InetAddress mine;
		public InetAddress all;
	}

	
}
