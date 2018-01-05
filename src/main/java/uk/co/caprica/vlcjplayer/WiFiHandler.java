package uk.co.caprica.vlcjplayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;

public class WiFiHandler {
	public static final String SSID = "karaokemp";
	public static final String PASS = "bestcampinmidburn";
	static final Executor exec = new DefaultExecutor();
	static final CommandLine create = CommandLine.parse("netsh wlan set hostednetwork mode=allow ssid=" + SSID + " key=" + PASS);
	static final CommandLine start = CommandLine.parse("netsh wlan start hostednetwork");
	static final CommandLine status = CommandLine.parse("netsh wlan show hostednetwork");
	
	static Timer tm = new Timer();
	
	public static void createAdHoc(){
		exec.setStreamHandler(new PumpStreamHandler());
		try {
			exec.execute(create);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tm.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if (!isOn())
					start();
				
			}
		}, 300000, 100000);
	}
	
	public static boolean isOn(){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		exec.setStreamHandler(new PumpStreamHandler(out));
		try {
			exec.execute(status);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.setStreamHandler(new PumpStreamHandler());
		return out.toString().contains("Started");
	}
	
	public static void start(){
		try {
			exec.execute(start);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
