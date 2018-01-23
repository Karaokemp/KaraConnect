package uk.co.caprica.vlcjplayer.service;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.mortbay.jetty.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Cleanup;
import lombok.Data;
import lombok.Value;
import lombok.val;
import spark.servlet.SparkApplication;
import uk.co.caprica.vlcjplayer.songlistreader.DB;
import uk.co.caprica.vlcjplayer.songlistreader.KaraokeReader;
import uk.co.caprica.vlcjplayer.view.playlist.PlaylistFrame;

public class RestService implements SparkApplication{
	public static final Logger LOGGER = Logger.getLogger(RestService.class.getName());
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	static {
		if (!Boolean.TRUE.toString().equals(System.getProperty("KARAOKEMP_ON_THE_CLOUD"))) {
			restInit();
		}
	}
	
	public static void main(String[] args) {
		
	}
	
	public static String reqS(HttpServletRequest re){
		try{
			@Cleanup val br = new BufferedReader(new InputStreamReader(re.getInputStream(),"UTF-8"));
			val song = br.readLine().trim();
			val file = br.readLine().trim();
			val perf = br.readLine().trim();
			val email = br.readLine().trim();
			val perfNumber = PlaylistFrame.getNextSerial();
			return PlaylistFrame.addSong(false, 
										 perf, 
										 song, 
										 file, 
										 String.valueOf(new Date().getTime()), 
										 email, 
										 new PlaylistFrame.WaitingTime("00:00:00"),
										 perfNumber,
										 0l) + ";" + perfNumber;
		}
		catch (Exception e){
			e.printStackTrace();
			return "-1";
		}
	}
	
	
	@Data
	public static class Req {
		public int songIndex;
		public String performerName;
		public String email;
	}
	
	@Value
	public static class Res {
		public Integer performerNumber;
		public int numberInLine;
	}
	
	public static void restInit()  {
		
		
		get("/KaraConnect/testConnection", (req, res) -> "Hello World");

		
		post("/KaraConnect/song/:songName", (req, res) -> {
			return RestService.reqS(req.raw());
		});
		
		get("/KaraConnect/songsList", (req, res) -> {
			res.raw().setContentType("application/octet-stream");
			res.raw().setHeader("Content-Disposition","attachment; filename=" + KaraokeReader.SONGS_LIST_FILE);
			val file = Files.readAllBytes(Paths.get(KaraokeReader.SONGS_LIST_FILE));
			res.raw().getOutputStream().write(file);
			res.raw().getOutputStream().flush();
			res.raw().getOutputStream().close();
			return res.raw();
		});
		
		post("/KaraConnect/signalReady", (req, res) -> {
			PlaylistFrame.signalReadyToSing();
			return true;
		});
		
		get("/KaraConnect/signalReady", (req, res) -> {
			return new String(Files.readAllBytes(Paths.get("ready.html")), Charset.forName("UTF-8"));
		});
		
		get("/api/find", (req, res) -> {
			val query = req.queryParams("query");
			res.header(HttpHeaders.CONTENT_TYPE, "application/json");
			LOGGER.log(Level.FINER, "query: " + query);
			return mapper.writeValueAsString(DB.search(query));
		});
		
		post("/api/requestSong", "application/json", (req, res) -> {
			val reqBody = mapper.readValue(new String(req.bodyAsBytes(), "UTF-8"), Req.class);
			val songFile = DB.getSong(reqBody.songIndex);
			val perfNumber = PlaylistFrame.getNextSerial();
			val numberInline = PlaylistFrame.addSong(false,
													 reqBody.performerName,
													 DB.cutSongName(songFile),
													 songFile,
													 String.valueOf(new Date().getTime()),
													 StringUtils.isEmpty(reqBody.email) ? "none" : reqBody.email,
													 new PlaylistFrame.WaitingTime("00:00:00"),
													 perfNumber,
													 0l);
			res.header(HttpHeaders.CONTENT_TYPE, "application/json");
			return mapper.writeValueAsString(new Res(StringUtils.isEmpty(perfNumber) ? null : Integer.parseInt(perfNumber), numberInline));
			
		});
		
		options("/*", (request, response) -> {

	        String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
	        if (accessControlRequestHeaders != null) {
	            response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
	        }

	        String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
	        if (accessControlRequestMethod != null) {
	            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
	        }

	        return "OK";
	    });

	    before((request, response) -> {
	        response.header("Access-Control-Allow-Origin", "*");
	    });
	    

	    get("/", (req, res) -> {
	        res.raw().getOutputStream().write(Files.readAllBytes(Paths.get("html/index.html")));
            			res.raw().getOutputStream().flush();
            			res.raw().getOutputStream().close();
			return res.raw();
	    });
	    
	    get("/:file", (req, res) -> {
	    	String file = (String)req.params(":file");
	    	if (!new File("html/" + file).exists()){
	    		file = "index.html";
	    	}
	    	val ext = file.substring(file.lastIndexOf(".") + 1, file.length());
	    	switch (ext) {
			case "html":
				res.raw().setContentType("text/html");
				break;
			case "css":
				res.raw().setContentType("text/css");
				break;
			case "woff2":
				res.raw().setContentType("font/woff2");
				break;
			default:
				break;
			}
	    	LOGGER.log(Level.FINER, "file: " + file);
	    	LOGGER.log(Level.FINER, "SPARKPATH " + RestService.class.getClassLoader().getResource("").getPath());
			res.raw().getOutputStream().write(Files.readAllBytes(Paths.get("html/" + file)));
			res.raw().getOutputStream().flush();
			res.raw().getOutputStream().close();
			return res.raw();
	    });
	}

	@Override
	public void init() {
		restInit();
	}
}
