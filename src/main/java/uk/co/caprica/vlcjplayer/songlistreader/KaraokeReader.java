package uk.co.caprica.vlcjplayer.songlistreader;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.DeleteDbFiles;

public class KaraokeReader {

	static List<String> songsList;
	public static String SONGS_LIST_FILE = "SongsList.csv";
	
	public static void readSongs(String rootDir) throws Exception {
		songsList = new ArrayList<String>(30300);
		File root = new File(rootDir);
		DeleteDbFiles.execute(".", DB.DB_NAME, true);
		
		readSongs(root);
		
		PrintStream ps = new PrintStream(SONGS_LIST_FILE, "UTF8");
		
		for (String curSong : songsList)
		{
			ps.println(curSong);
		}
		
		ps.close();
		
		DB.insertFiles(songsList);
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
