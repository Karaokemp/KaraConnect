package uk.co.caprica.vlcjplayer.view.playlist;

import static uk.co.caprica.vlcj.player.Logo.logo;

import java.util.Timer;
import java.util.TimerTask;

import gui.ava.html.Html2Image;
import uk.co.caprica.vlcj.binding.internal.libvlc_logo_position_e;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class KaraLogo {
	static Timer timer = new Timer();
	public static void createLogo(String songName, String performer, MediaPlayer player){
		Html2Image.fromHtml("<h3>" + performer + " - " + songName + "</h3>").getImageRenderer().saveImage("logoTemp.png");
		logo().file("logoTemp.png").position(libvlc_logo_position_e.bottom).opacity(50).enable(true).apply(player);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				player.enableLogo(false);
				
			}
		}, 10000);
	}
	
	public static void main(String[] args){
		createLogo("abba money", "daniel", null);
	}

}
