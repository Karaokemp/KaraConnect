/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015 Caprica Software Limited.
 */

package uk.co.caprica.vlcjplayer;

import static uk.co.caprica.vlcjplayer.Application.application;

import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.log.NativeLog;
//import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.streams.NativeStreams;
import uk.co.caprica.vlcjplayer.service.Broadcaster;
import uk.co.caprica.vlcjplayer.service.RestService;
import uk.co.caprica.vlcjplayer.view.debug.DebugFrame;
import uk.co.caprica.vlcjplayer.view.effects.EffectsFrame;
import uk.co.caprica.vlcjplayer.view.main.MainFrame;
import uk.co.caprica.vlcjplayer.view.messages.NativeLogFrame;
import uk.co.caprica.vlcjplayer.view.playlist.KaraWindowAdapter;
import uk.co.caprica.vlcjplayer.view.playlist.PlaylistFrame;

/**
 * Application entry-point.
 */
public class VlcjPlayer {

	private static final NativeStreams nativeStreams;

	// Redirect the native output streams to files, useful since VLC can
	// generate a lot of noisy native logs we don't care about
	// (on the other hand, if we don't look at the logs we might won't see
	// errors)
	static {
		// if (RuntimeUtil.isNix()) {
		// nativeStreams = new NativeStreams("stdout.log", "stderr.log");
		// }
		// else {
		nativeStreams = null;
		// }
	}

	private final JFrame mainFrame;

	@SuppressWarnings("unused")
	private final JFrame messagesFrame;

	@SuppressWarnings("unused")
	private final JFrame effectsFrame;

	@SuppressWarnings("unused")
	private final JFrame debugFrame;

	@SuppressWarnings("unused")
	private final JFrame playlistFrame;

	private final NativeLog nativeLog;

	@SuppressWarnings("unused")
	private final Broadcaster broadcaster = new Broadcaster();

	public static void main(String[] args) {
		// This will locate LibVLC for the vast majority of cases
		if (SystemUtils.IS_OS_WINDOWS) {
			if (StringUtils.isEmpty(System.getProperty("VLC_PLUGIN_PATH"))){
				System.getProperties().setProperty("VLC_PLUGIN_PATH", "c:/Program Files/VideoLAN/VLC/plugins");
			}
				if (System.getenv("ENABLE_ADHOC") != null && System.getenv("ENABLE_ADHOC").equalsIgnoreCase("true")){
				WiFiHandler.createAdHoc();
				WiFiHandler.start();
			}
		}
		
		new NativeDiscovery().discover();
		RestService.init();
		

		setLookAndFeel();
		Data.relaunch = args.length > 0;

		load();
	}

	public static void load() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new VlcjPlayer().start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private static void setLookAndFeel() {
//		String lookAndFeelClassName;
//		if (RuntimeUtil.isNix()) {
//			lookAndFeelClassName = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
//		} else {
//			lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
//		}
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			// Silently fail, it doesn't matter
		}
	}

	public VlcjPlayer() {

		mainFrame = new MainFrame();
		mainFrame.addWindowListener(new KaraWindowAdapter(mainFrame));
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		EmbeddedMediaPlayerComponent mediaPlayerComponent = application().mediaPlayerComponent();

		nativeLog = mediaPlayerComponent.getMediaPlayerFactory().newLog();

		messagesFrame = new NativeLogFrame(nativeLog);
		effectsFrame = new EffectsFrame();
		debugFrame = new DebugFrame();
		playlistFrame = new PlaylistFrame();
	}

	public static void release() {
		try {
			EmbeddedMediaPlayerComponent mediaPlayerComponent = application().mediaPlayerComponent();
			mediaPlayerComponent.getMediaPlayer().stop();
			mediaPlayerComponent.release();
		} catch (Error e) {
			PlaylistFrame.LOGGER.log(Level.SEVERE, "fuck", e);
		}
		if (nativeStreams != null) {
			nativeStreams.release();
		}
	}

	private void start() {
		mainFrame.setVisible(true);
	}
}
