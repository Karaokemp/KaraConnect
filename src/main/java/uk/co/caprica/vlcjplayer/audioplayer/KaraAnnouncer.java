package uk.co.caprica.vlcjplayer.audioplayer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import lombok.Cleanup;
//import sun.audio.AudioPlayer;
//import sun.audio.AudioStream;
import uk.co.caprica.vlcjplayer.view.playlist.PlaylistFrame;

public class KaraAnnouncer {
	private static final String FOLDER = "announcements/";
	private static byte[] englishWelcome;
	private static byte[] hebrewWelcome;
	private static byte[][] enNumbers = new byte[10][];
	private static byte[][] heNumbers = new byte[10][];
	// private static byte[] getReady;

	static {
		try {
			englishWelcome = Files.readAllBytes(Paths.get(FOLDER + "enWelcome.wav"));
			hebrewWelcome = Files.readAllBytes(Paths.get(FOLDER + "heWelcome.wav"));
			// getReady = Files.readAllBytes(Paths.get(FOLDER +
			// "getReady.wav"));

			for (int i = 0; i <= 9; i++) {
				enNumbers[i] = Files.readAllBytes(Paths.get(FOLDER + "en" + i + ".wav"));
				heNumbers[i] = Files.readAllBytes(Paths.get(FOLDER + "he" + i + ".wav"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void announce(String perfNumber) {
		playWav(hebrewWelcome);
		playNumber(perfNumber, heNumbers);
		playWav(englishWelcome);
		playNumber(perfNumber, enNumbers);
	}

	// public static void getReady(){
	// playWav(getReady);
	// }

	private static void playNumber(String perfNumber, byte[][] numbers) {
		for (int i = 0; i < 3; i++) {
			playWav(numbers[Integer.valueOf(perfNumber.substring(i, i + 1))]);
		}
	}

	private static void playWav(byte[] wav) {
		try {
			@Cleanup
			AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wav));
			Clip clip = AudioSystem.getClip();

			clip.open(ais);
			FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-12.0f); // Reduce volume by 10 decibels.

			clip.start();

			while (!clip.isRunning())
				Thread.sleep(10);
			while (clip.isRunning())
				Thread.sleep(10);

			clip.close();

		} catch (Exception e) {
			PlaylistFrame.LOGGER.log(Level.SEVERE, "error playing wav", e);
		}
	}

	public static void main(String[] args) throws Exception {
		announce("123");
		// open the sound file as a Java input stream
		// String gongFile = "D:/Downloads/a2002011001-e02-ulaw.wav";
		// InputStream in = new FileInputStream(gongFile);

		// create an audiostream from the inputstream
		// AudioStream audioStream = new AudioStream(in);

		// play the audio clip with the audioplayer class
		// AudioPlayer.player.start(audioStream);
		// Thread.sleep(2000);
		// AudioPlayer.player.
	}

}
