package uk.co.caprica.vlcjplayer.view.playlist;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.JOptionPane;

import lombok.val;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

public class KaraRecorder {
	static final String exec_dir = "ffmpeg";
	static final String CMD_FILE = "recordingCommand.txt";

	String outFile;

	PrintStream printer;

	FFmpeg ffmpeg;
	FFprobe ffprobe;

	public KaraRecorder() throws RecorderNotInitializedException {

		try {
		if (SystemUtils.IS_OS_WINDOWS) {
			ffmpeg = new FFmpeg(exec_dir + "/ffmpeg");
			ffprobe = new FFprobe(exec_dir + "/ffprobe");
		}
			// pb.
		} catch (IOException e) {
			throw new RecorderNotInitializedException();
		}
	}

	public void startRecording(String video, String audio, String output, long duration) {
		outFile = output;
		new Thread() {

			@Override
			public void run() {
				FFmpegBuilder builder = new FFmpegBuilder()

						.setInput("video=" + video + ":audio=" + audio + "") // Filename,
																				// or
																				// a
																				// FFmpegProbeResult
						.overrideOutputFiles(true) // Override the output if it
													// exists
						.setFormat("dshow").setFramerate("25").setVcodec("h264").setVideoSize("1280x720")
						.addOutput(output) // Filename for the destination

						.disableSubtitle() // No subtiles

						.setAudioChannels(1) // Mono audio
						.setAudioCodec("aac") // using the aac codec
						.setAudioSampleRate(44_100) // at 48KHz
						.setAudioBitRate(160000) // at 32 kbit/s
						.setDuration(duration, TimeUnit.MILLISECONDS)
						.setVideoCodec("copy") // Video using x264
						.setVideoCopyInkf(true).setThreads(2).setAsync(1)
						.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow
																		// FFmpeg
																		// to
																		// use
																		// experimental
																		// specs
						.done();

				FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
				executor.createJob(builder).run();

			}
		}.start();
	}

	public void stopRecording() {

		new Thread() {

			@Override
			public void run() {

				try {
					ffmpeg.getProcessIn().write("q");
					ffmpeg.getProcessIn().flush();
				} catch (IOException e) {
					PlaylistFrame.LOGGER.log(Level.SEVERE, "Error with recording", e);
					JOptionPane.showMessageDialog(PlaylistFrame.instance, "Error Recording! \n" + e.getMessage());
				}
			}
		}.start();
	}

	public static class RecorderNotInitializedException extends Exception {
		private static final long serialVersionUID = -8067000383774750143L;

	}

	public static void main(String[] args) throws RecorderNotInitializedException, InterruptedException {
		val rec = new KaraRecorder();
		// FFmpegBuilder builder = new
		// FFmpegBuilder().addExtraArgs("-list_devices",
		// "true").setFormat("dshow").addInput("dummy").addOutput("out.mp4").done();
		// FFmpegExecutor executor = new FFmpegExecutor(rec.ffmpeg,
		// rec.ffprobe);
		// executor.createJob(builder).run();
		rec.startRecording("HD Pro Webcam C920", "Microphone (HD Pro Webcam C920)", "RecordingTestResult.mp4", 30000);
		Thread.sleep(8000);
		rec.stopRecording();
	}
}
