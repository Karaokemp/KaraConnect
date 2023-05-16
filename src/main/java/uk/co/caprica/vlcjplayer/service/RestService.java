package uk.co.caprica.vlcjplayer.service;
import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.mortbay.jetty.HttpHeaders;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Cleanup;
import lombok.Data;
import lombok.Value;
import lombok.val;
import uk.co.caprica.vlcjplayer.songlistreader.DB;
import uk.co.caprica.vlcjplayer.songlistreader.KaraokeReader;
import uk.co.caprica.vlcjplayer.view.playlist.PlaylistFrame;

public class RestService {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	static {
		port(80);
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
		
		get("/find", (req, res) -> {
			val query = req.queryParams("query");
			res.header(HttpHeaders.CONTENT_TYPE, "application/json");
			System.out.println(query);
			return mapper.writeValueAsString(DB.search(query));
		});
		
		post("/requestSong", "application/json", (req, res) -> {
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
//	        response.header("Access-Control-Request-Method", methods);
//	        response.header("Access-Control-Allow-Headers", headers);
	        // Note: this may or may not be necessary in your particular application
//	        response.type("application/json");
	    });

	    get("/", (req, res) -> {
	        res.raw().getOutputStream().write(Files.readAllBytes(Paths.get("html/index.html")));
            			res.raw().getOutputStream().flush();
            			res.raw().getOutputStream().close();
            			return res.raw();
	    });
	    
	    get("/:file", (req, res) -> {
	    	System.out.println();
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
	    	System.out.println(file);
			res.raw().getOutputStream().write(Files.readAllBytes(Paths.get("html/" + file)));
			res.raw().getOutputStream().flush();
			res.raw().getOutputStream().close();
			return res.raw();
//	    	return new String(Files.readAllBytes(Paths.get(RestService.class.getResource("/html/" + file).getPath().substring(1))), Charset.forName("UTF-8"));
	    });
	}
	
	public static void main(String[] args) throws IOException{
//		System.out.println(RestService.class.getResource("/html/index.html").getPath().substring(1));
//		System.out.println(Files.readAllBytes(Paths.get(RestService.class.getResource("/html/index.html").getPath().substring(1))));
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
	
	public static void init(){};
	
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
}
