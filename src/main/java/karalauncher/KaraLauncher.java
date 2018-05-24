package karalauncher;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import uk.co.caprica.vlcjplayer.VlcjPlayer;

public class KaraLauncher extends JDialog{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2091513657677171666L;
	static String java_home = "jdk";
	static String exec_dir = ".";
	public static void main(String[] args) {
//		new JFrame();
		if (args.length > 0)
			java_home = args[0];
		if (args.length > 1)
			exec_dir = args[1];
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				new KaraLauncher();
			}
		});
		
    }
	JButton launchKill;
	JLabel label;
	DefaultExecutor executor;
	public KaraLauncher(){
		
		
		executor = new DefaultExecutor();
		executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
		executor.setStreamHandler(new PumpStreamHandler());
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new KaraWindowAdapter(this, false));
        // Set's the window to be "always on top"
        this.setAlwaysOnTop( true );
        this.setResizable(false);
        
//        this.windwo
        this.setLocationByPlatform( true );
        this.setLayout(new GridLayout(2, 1));
        label = new JLabel("Karaoke DJ Not Running");
        label.setSize(200, 35);
        launchKill = new JButton("Launch Karaoke DJ");
        launchKill.setSize(200, 35);
        launchKill.addActionListener(new ActionListener() {
			
			
			public void actionPerformed(ActionEvent e) {
				try{
					getProcess();
				}
				catch (Exception ex){
					ex.printStackTrace();
				}
			}
		});
        getContentPane().add( label );
        getContentPane().add(launchKill);
        this.pack();
        this.setSize(200, 70);
        this.setVisible( true );
        
        
        
	}
	
	void getProcess() {
		boolean relaunch = false;
				try {
					ShutdownHookProcessDestroyer pd = (ShutdownHookProcessDestroyer)executor.getProcessDestroyer();
					if (pd.size() > 0){
						relaunch = true;
						pd.run();
						Thread.sleep(7000);
					}
					String line = "java -Dfile.encoding=utf-8 -cp kara-vlc-1.0.0-SNAPSHOT-jar-with-dependencies.jar " + VlcjPlayer.class.getName()  + (relaunch ? " relaunch" : "");
					CommandLine cmdLine = CommandLine.parse(line);
					executor.execute(cmdLine, new ExecuteResultHandler() {
						
						public void onProcessFailed(ExecuteException e) {
							onProcessComplete(-1);				
						}
						
						public void onProcessComplete(int exitValue) {
							launchKill.setText("Launch");		
							label.setText("Karaoke DJ Not Running");
						}
					});
					label.setText("Karaoke DJ is Running");
					launchKill.setText("Kill and Relaunch");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	}
}
