package uk.co.caprica.vlcjplayer.songlistreader;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.DeleteDbFiles;

import javax.swing.*;

public class KaraokeReader {

	static List<String> songsList;
	public static String SONGS_LIST_FILE = "SongsList.csv";
	
	public static void readSongs(String rootDir, boolean flushExistingDataFirst) throws Exception {
		songsList = new ArrayList<String>(30300);
		File root = new File(rootDir);

		JOptionPane msg = new JOptionPane("Working. This may take a while.", JOptionPane.INFORMATION_MESSAGE);
		final JDialog dlg = msg.createDialog("Scanning root directory "+(flushExistingDataFirst ? "from scratch":"for new songs")+"...");
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		new Thread(new Runnable() {
			@Override
			public void run() { dlg.setVisible(true); }
		}).start();

		if (flushExistingDataFirst) DeleteDbFiles.execute(".", DB.DB_NAME, true);
		DB.init();
		
		readSongs(root);
		
		PrintStream ps = new PrintStream(SONGS_LIST_FILE, "UTF8");
		
		for (String curSong : songsList)
		{
			ps.println(curSong);
		}
		
		ps.close();

		DB.insertSongs(songsList, true);

		dlg.setVisible(false);
	}
	
	static void readSongs(File curDir)
	{
		File[] filesList = curDir.listFiles(new KaraFilter()); 
		
		if (filesList != null)
		for (File curFile : filesList)
		{
			if (curFile.isDirectory())
			{
				readSongs(curFile);
			}
			else
			{
				songsList.add(curFile.getPath());
			}
		}
	}
	
	public static class KaraFilter implements FileFilter{
		protected boolean acceptDir = true;
		protected String[] extensions = new String[]{"mp3", "mp4", "avi", "mpg", "mpeg", "mkv", "wmv", "webm", "flac", "ape", "mpc", "wav", "wma", "m4v"};
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return acceptDir;
			
			String name = pathname.getName();
			for (int i = 0, n = extensions.length; i < n; i++) {
		        String extension = extensions[i];
		        if ((name.endsWith(extension) && (name.charAt(name.length() 
		                  - extension.length() - 1)) == '.')) {
		          return true;
		        }
		      }
			
			return false;
		}
	}
	
}
