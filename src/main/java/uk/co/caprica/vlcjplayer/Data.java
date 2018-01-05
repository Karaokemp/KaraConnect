package uk.co.caprica.vlcjplayer;

public class Data {
	public final static String playlistFileName = "KaraConnectPlaylist.kcp";
	public final static String gaplistFileName = "GapPlaylist.kcp";
	public final static String settingsFileName = "DjPlaylist.properties";
	public final static String actionLogFileName = "Actions.log";
	public final static String errorLogFileName = "Errors.log";
	public static String mediaRootFolder = "/media/ophir/";
	public static boolean relaunch = false;
//	public static List<SongRequest> karaokePlaylist = Collections.synchronizedList(new ArrayList<SongRequest>());
//	public static List<String> gapPlaylist = new ArrayList<>();
	
	public static class SongRequest
	{
		public String fileName;
		public String songName;
		public String performerName;
		public long date;
		public boolean record;
		public String email;
		public boolean played;
		
		public Object[] row;

		public SongRequest(String fileName, String songName, String performerName, long date, boolean record,
				String email, boolean played) {
			super();
			this.fileName = fileName;
			this.songName = songName;
			this.performerName = performerName;
			this.date = date;
			this.record = record;
			this.email = email;
			this.played = played;
		}
		
		
	}
}
