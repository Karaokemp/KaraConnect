package uk.co.caprica.vlcjplayer.view.playlist;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import uk.co.caprica.vlcjplayer.service.Broadcaster;

public class ParticipantControl {
	static final int CAM_PORT = 8878;
	static final int RELOAD_PORT = 8879;
	static Timer tm = new Timer();
	
	public static void jumpStart(){
		tm.schedule(new TimerTask() {
			
			@Override
			public void run() {
				Broadcaster.sendMessage(PlaylistFrame.instance.isCamEnabled() ? "ON" : "OFF", CAM_PORT);
				
			}
		}, 1000, 60000);
	}
	
	public static void reloadParticipantPlaylist(){
		new Thread() {
			
			@Override
			public void run() {
				try {
				Broadcaster.sendMessage("Reload", RELOAD_PORT);
				} catch (Exception e) {
					PlaylistFrame.LOGGER.log(Level.SEVERE, "Exception", e);
				}
				
			}
		}.start();
	}
}
