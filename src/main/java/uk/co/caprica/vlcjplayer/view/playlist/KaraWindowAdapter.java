package uk.co.caprica.vlcjplayer.view.playlist;

import static uk.co.caprica.vlcjplayer.Application.application;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

import lombok.val;
import uk.co.caprica.vlcjplayer.VlcjPlayer;
import uk.co.caprica.vlcjplayer.event.ShutdownEvent;

public class KaraWindowAdapter extends WindowAdapter {

	private Component parent;
	boolean ask = true;

	public KaraWindowAdapter(Component parent, boolean ask) {
		this.parent = parent;
		this.ask = ask;
	}

	public KaraWindowAdapter(Component parent) {
		this.parent = parent;
	}

	public void windowClosing(WindowEvent e) {
		if (ask) {
			val answer = JOptionPane.showConfirmDialog(parent, "Are you sure you want to exit?", "Exit Application",
					JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION) {
				PlaylistFrame.release();
				VlcjPlayer.release();
				application().post(ShutdownEvent.INSTANCE);
				System.exit(0);
			}
		}
	}
}
