package uk.co.caprica.vlcjplayer.songlistreader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.h2.jdbcx.JdbcConnectionPool;

import lombok.Cleanup;
import lombok.val;
import uk.co.caprica.vlcj.player.MediaMeta;

import static uk.co.caprica.vlcjplayer.Application.application;

// H2 Database ConnectionPool Example
public class DB {

	public static final Logger LOGGER = Logger.getLogger(DB.class.getName());

	public static final String DB_NAME = "songsDB";
	private static final String DB_DRIVER = "org.h2.Driver";
	private static final String DB_CONNECTION = "jdbc:h2:./" + DB_NAME + ";CACHE_SIZE=300192";
	private static final String DB_USER = "";
	private static final String DB_PASSWORD = "";
	private static final String INSERT = "INSERT INTO SONGS VALUES(%d, '%s', '%s', %d, %d, %s, %d, %d)";

	private static boolean dumpForDebugEnable = true;

	public static int popularityPlaycountThreshold = 5;
	public static int lastPlayedMinutesDisplayThreshold = 30;

	private static final JdbcConnectionPool pool = getConnectionPool();

	public static int getSongId(String fileWithPath) {
		return fileWithPath.hashCode();
	}

	public static void insertSongs(String... files) {
		insertSongs(Arrays.asList(files), false);
	}

	public static void insertSongs(List<String> files, boolean removeDBOrphanSongs) {
		try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();

			if (removeDBOrphanSongs) stmt.execute("UPDATE SONGS SET flag=1");

			for (val file : files) {
				int timeInSecs = getMediaFileLengthInSeconds(file);
				stmt.addBatch(String.format(INSERT, getSongId(file), StringEscapeUtils.escapeSql(file), StringEscapeUtils.escapeSql(cutSongName(file)), timeInSecs, 0, "null", 0, 0));
				//LOGGER.info(String.format(INSERT, getSongId(file), StringEscapeUtils.escapeSql(file), timeInSecs, 0, "null", 0));

				if (removeDBOrphanSongs) stmt.addBatch(String.format("UPDATE SONGS SET flag=0 WHERE ID = %d",getSongId(file)));
			}

			stmt.executeBatch();

			if (removeDBOrphanSongs) stmt.execute("DELETE FROM SONGS WHERE flag=1");

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}


	public static void clean() {
		try {
			pool.dispose();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void init() {
		try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS SONGS(id int primary key, file varchar, name varchar, length_sec smallint, played_count int, last_played datetime, marked_as_bad bool, flag int)");
			stmt.execute("CREATE INDEX IF NOT EXISTS SONG_INDEX ON SONGS(name)");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static int getMediaFileLengthInSeconds(String fileName) {
		try {
			boolean loadedOK = application().mediaPlayerComponent().getMediaPlayer().prepareMedia(fileName);
			application().mediaPlayerComponent().getMediaPlayer().parseMedia();
			MediaMeta mediaMeta = application().mediaPlayerComponent().getMediaPlayer().getMediaMeta();

			//LOGGER.info(mediaMeta);

			long durationInSeconds = mediaMeta.getLength()/1000;
            return (int) durationInSeconds;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return 0;
	}

    public static Map<Integer, String> search(String terms) {
		dumpForDebug();

    	val termList = Arrays.asList(terms.split(" ")).stream().map(term -> " REGEXP_LIKE(NAME, '.*" + StringEscapeUtils.escapeSql(term) + ".*', 'i')").collect(Collectors.toList());
    	val files = new HashMap<Integer, String>();
    	try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
            
			ResultSet set = stmt.executeQuery("SELECT *, DATEDIFF('MINUTE', last_played, CURRENT_TIMESTAMP()) AS minutes_since_last_played FROM SONGS WHERE " + String.join(" AND ", termList) + " AND marked_as_bad=0 ORDER BY played_count DESC");
            while (set.next()) {
            	int timeInSecs = set.getInt("length_sec");
				int timeInMins = timeInSecs/60;
				int timeInSecsLeft = timeInSecs - (60*timeInMins);
				boolean popular = (set.getInt("played_count")>popularityPlaycountThreshold);
				int minutesLastPlayed = (set.getString("last_played")==null) ? 9999999 :
										 set.getInt("minutes_since_last_played");

            	files.put(set.getInt("ID"), set.getString("name") +
														(timeInSecs>0 ? "  ("+timeInMins+":"+String.format("%02d", timeInSecsLeft)+")" : "") +
														(minutesLastPlayed< lastPlayedMinutesDisplayThreshold ? " (played just "+minutesLastPlayed+" minutes ago) " : "") +
														(popular ? "  ***popular***" : "")
				);
				//files.put(set.getInt("ID"), cutSongName(set.getString("NAME")));
            	LOGGER.info(set.getString("ID") + " - " + set.getString("file"));
            }
            
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
    	
    	return files;
    }


	public static Map<Integer, String> searchTop() {
		dumpForDebug();

		val files = new HashMap<Integer, String>();
		try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();

			ResultSet set = stmt.executeQuery("SELECT * FROM (SELECT *, (played_count + RAND(100) + (CASE WHEN played_count>0 THEN 100000 ELSE 0 END)) AS played_count_with_rand FROM SONGS WHERE (last_played IS NULL OR DATEDIFF('MINUTE', last_played, CURRENT_TIMESTAMP())>45) AND marked_as_bad=0) ORDER BY played_count_with_rand DESC LIMIT 30");
			while (set.next()) {
				int timeInSecs = set.getInt("length_sec");
				int timeInMins = timeInSecs/60;
				int timeInSecsLeft = timeInSecs - (60*timeInMins);

				files.put(set.getInt("ID"), set.getString("name") +
						(timeInSecs>0 ? "  ("+timeInMins+":"+String.format("%02d", timeInSecsLeft)+")" : "")
				);
				//LOGGER.info(set.getString("ID") + " - " + set.getString("file"));
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

		return files;
	}

	public static void dumpForDebug() {
		if (!dumpForDebugEnable) return;
		try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();

			ResultSet set = stmt.executeQuery("SELECT * FROM SONGS");
			while (set.next()) {
				String rowText = "";
				for (int i=0 ; i<8; i++) {
					Object obj = set.getObject(i+1);
					if (obj==null) obj = "null";
					rowText += obj.toString() + " ";
				}
				LOGGER.info("DB Dump: "+rowText);
			}

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}


	public static void updateThatSongWasSelected(int songId) {
		//LOGGER.info(String.format("updateThatSongWasPlayed(): UPDATE SONGS SET played_count=played_count+1, last_played=CURRENT_TIMESTAMP() WHERE ID = %d", songId));
		try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
			stmt.execute(String.format("UPDATE SONGS SET played_count=played_count+1, last_played=CURRENT_TIMESTAMP() WHERE ID = %d", songId));
			updatePopularityThreshold();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void updatePopularityThreshold() {
		try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
			ResultSet set = stmt.executeQuery("SELECT played_count FROM SONGS ORDER BY played_count DESC LIMIT 1");
			if (set.next()) {
				int max_count =  set.getInt("played_count");
				popularityPlaycountThreshold = Math.max(5, Math.round(max_count/2));
				LOGGER.info("decidePopularityThreshold(): popularityPlaycountThreshold is set to "+popularityPlaycountThreshold);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void markSongAsBad(int songId) {
		try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
			stmt.execute(String.format("UPDATE SONGS SET marked_as_bad=1 WHERE ID = %d", songId));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static String getSong(int songId) {
    	try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
			ResultSet set = stmt.executeQuery(String.format("SELECT file FROM SONGS WHERE id = %d", songId));
            if (set.next()) {
            	return set.getString("file");
            }
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
    	
    	return null;
    }

    
    public static Connection getConnection() {
    	try {
			return pool.getConnection();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
    }
    
    public static String cutSongName(String rawName)
    {
        val split = rawName.split("[/\\\\]");
        val splitted = split[split.length - 1];
        return splitted.substring(0, splitted.lastIndexOf("."));
    }

    // Create H2 JdbcConnectionPool
    private static JdbcConnectionPool getConnectionPool() {
        JdbcConnectionPool cp = null;
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        cp = JdbcConnectionPool.create(DB_CONNECTION, DB_USER, DB_PASSWORD);
        return cp;
    }
}