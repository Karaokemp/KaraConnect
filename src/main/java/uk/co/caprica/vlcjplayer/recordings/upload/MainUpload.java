package uk.co.caprica.vlcjplayer.recordings.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.mail.MessagingException;

import lombok.Cleanup;
import lombok.val;
import uk.co.caprica.vlcjplayer.songlistreader.KaraokeReader.KaraFilter;

public class MainUpload {
	
	private static final String SUCCESS = "/SuccessfulUploads";
	static final int EMAIL = 0;
	static final int PERFORMER = 1;
	static final int SONG = 2;
	static final int TIME = 3;
	static final int VIDEO_ID = 4;
	public static void main(String[] args) throws IOException, Exception {
		@Cleanup BufferedReader enc = new BufferedReader(new InputStreamReader(new ProcessBuilder("chcp.com").start().getInputStream()));
		val charset = enc.readLine().split(":")[1].trim();
		String encoding = "IBM862";
		for (val set : Charset.availableCharsets().keySet())
			if (set.contains(charset)){
				System.out.println(set);
				encoding = set;
				break;
			}
		@Cleanup BufferedReader br = new BufferedReader(new InputStreamReader(System.in, encoding));
		System.out.println("Enter recording folder (empty will take from dj preferences):");
		String folder = br.readLine();
		String eventName = "";
		if (folder.equals("")){
			@Cleanup val br2 = new BufferedReader(new FileReader("uploadPreferences.properties"));
//			br.readLine();
			folder = br2.readLine();
			eventName = br2.readLine();
		}
		else{
			System.out.println("Enter event name (hebrew name,english name):");
			eventName = br.readLine();
		}
		System.out.println(eventName);
		System.out.println("Enter email password:");
		String pass = br.readLine();
		uploadFolder(folder, eventName, pass);
	}
	
	public static void uploadFolder(String folder, String eventName, String pass) throws IOException{
		String success = folder + SUCCESS;
		new  File(success).mkdirs();
		File[] filesList = new File(folder).listFiles(new UploadFilter());
		String body = new String(Files.readAllBytes(Paths.get("EmailBody.upload")), "UTF-8");
		
		for (File curFile : filesList){
//			val fileName = curFile.getName();
			val eventNames = eventName.split(",");
			String[] meta = new String[5];
			try {
			readMeta(curFile.getPath(), meta, "email");
			}
			catch (IOException e){
				readMeta(curFile.getPath(), meta, "emailProblem");
				new File(curFile.getPath().replace("mp4", "emailProblem")).renameTo(new File(curFile.getPath().replace("mp4", "email")));
			}
			
			try {
				meta[VIDEO_ID] = meta[VIDEO_ID] == null ? UploadVideo.uploadVideo(curFile, meta, eventNames[1]) : meta[VIDEO_ID];
				String newBody = String.format(body, eventNames[0], eventNames[1], meta[VIDEO_ID]);
				
				try {
					
					SendMail.generateAndSendEmail(meta[EMAIL], String.format("Your %2$s Performance ההופעה שלך ב%1$s", eventNames[0], eventNames[1]), newBody, pass);
					Files.move(Paths.get(curFile.getPath()), Paths.get(success + "/" + curFile.getName()));
					Files.move(Paths.get(curFile.getPath().replace("mp4", "email")), Paths.get(success + "/" + curFile.getName().replace("mp4", "email")));
				} catch (MessagingException e) {
					writeMeta(curFile.getPath(), meta, "email", e.getMessage());
					new File(curFile.getPath().replace("mp4", "email")).renameTo(new File(curFile.getPath().replace("mp4", "emailProblem")));
					e.printStackTrace();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				continue;
			}
			
			
			
		}
	}
	
	private static void readMeta(String fileName, String[] meta, String extension) throws IOException{
		@Cleanup val br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName.replace("mp4", extension)), "UTF-8"));
		meta[EMAIL] = br.readLine();
		meta[PERFORMER] = br.readLine();
		meta[SONG] = br.readLine();
		meta[TIME] = br.readLine();
		meta[VIDEO_ID] = br.readLine();
	}
	
	private static void writeMeta(String fileName, String[] meta, String extension, String exception) throws IOException{
		@Cleanup val ps = new PrintStream(fileName.replace("mp4", extension), "UTF-8");
		for (val met : meta)
			ps.println(met);
		ps.println(exception);
	}
}

class UploadFilter extends KaraFilter{
	public UploadFilter(){
		acceptDir = false;
		extensions = new String[]{"mp4"};
	}
}
