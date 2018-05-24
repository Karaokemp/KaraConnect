package uk.co.caprica.vlcjplayer.service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import lombok.Cleanup;
import uk.co.caprica.vlcjplayer.view.playlist.PlaylistFrame;

public class Receiver {
	public static void start() throws Exception

	{
		new Thread(new Runnable() {

			@Override
			public void run() {
				@Cleanup DatagramSocket serverSocket = null;
				try {
					serverSocket = new DatagramSocket(8989);
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				byte[] receiveData = new byte[1024];

				while (true)

				{

					try {

						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

						serverSocket.receive(receivePacket);

						String sentence = new String(receivePacket.getData());

						System.out.println("RECEIVED: " + sentence);

						if (sentence.contains("Ready")) {
							PlaylistFrame.signalReadyToSing();
						}

					} catch (Exception e) {

						// TODO Auto-generated catch block

						e.printStackTrace();

					}

				}
			}
		}).start();

	}
}
