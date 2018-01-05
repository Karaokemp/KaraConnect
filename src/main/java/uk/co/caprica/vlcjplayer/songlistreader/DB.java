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

// H2 Database ConnectionPool Example
public class DB {
	
	public static final Logger LOGGER = Logger.getLogger(DB.class.getName());

	public static final String DB_NAME = "songsDB";
    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:./" + DB_NAME + ";CACHE_SIZE=300192";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";
    private static final String INSERT = "INSERT INTO SONGS VALUES(%d, '%s')";
    
    private static final JdbcConnectionPool pool = getConnectionPool();

    public static void insertFiles(String... files) {
    	insertFiles(Arrays.asList(files));
    }
    
    public static void insertFiles(List<String> files) {
    	try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
			for (val file : files) {
				stmt.addBatch(String.format(INSERT, file.hashCode(), StringEscapeUtils.escapeSql(file)));
			}
			
			stmt.executeBatch();
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
            stmt.execute("CREATE TABLE SONGS(id int primary key, name varchar)");
            stmt.execute("CREATE INDEX SONG_INDEX ON SONGS(name)");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
    }

    
    public static Map<Integer, String> search(String terms) {
    	val termList = Arrays.asList(terms.split(" ")).stream().map(term -> " REGEXP_LIKE(NAME, '.*" + StringEscapeUtils.escapeSql(term) + ".*', 'i')").collect(Collectors.toList());
    	val files = new HashMap<Integer, String>();
    	try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
            
			ResultSet set = stmt.executeQuery("SELECT * FROM SONGS WHERE " + String.join(" AND ", termList));
            while (set.next()) {
            	files.put(set.getInt("ID"), cutSongName(set.getString("NAME")));
            	LOGGER.info(set.getString("ID") + " - " + set.getString("NAME"));
            }
            
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
    	
    	return files;
    }
    
    public static String getSong(int id) {
    	try {
			@Cleanup val conn = getConnection();
			@Cleanup Statement stmt = conn.createStatement();
			ResultSet set = stmt.executeQuery(String.format("SELECT NAME FROM SONGS WHERE ID = %d", id));
            if (set.next()) {
            	return set.getString("NAME");
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