package uk.co.caprica.vlcjplayer.view.playlist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

public abstract class KaraAction implements ActionListener {
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			PlaylistFrame.ACTION_LOGGER.log(Level.INFO, e.paramString());
		} catch (Exception e1) {
			PlaylistFrame.LOGGER.log(Level.SEVERE, "Exception", e1);
		}
		SwingUtilities.invokeLater(new Runni(this));
	}
	
	public abstract void performAction();

}

class Runni implements Runnable{
	KaraAction action;
	public Runni(KaraAction action) {
		this.action = action;
	}

	@Override
	public void run() {
		action.performAction();
		
	}
	
}
