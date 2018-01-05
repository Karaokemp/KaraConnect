package uk.co.caprica.vlcjplayer.view.playlist;

import java.util.logging.Level;

import uk.co.caprica.vlcjplayer.service.Broadcaster;


public class OnAirControl {
	static final int PORT = 8899;
	public static void onAir(){
		new Thread() {
			
			@Override
			public void run() {
				try {
				Broadcaster.sendMessage("Close", PORT);
				
					Thread.sleep(2000);
				
				Broadcaster.sendMessage("Close", PORT);
				} catch (Exception e) {
					PlaylistFrame.LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
			}
		}.start();
		
	}
	
	public static void offAir(){
		new Thread() {
			
			@Override
			public void run() {
				try {
				Broadcaster.sendMessage("Open", PORT);
				
					Thread.sleep(2000);
				
				Broadcaster.sendMessage("Open", PORT);
				} catch (Exception e) {
					PlaylistFrame.LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
			}
		}.start();
	}
	
	public static void main(String[] args){
		onAir();
	}
}
