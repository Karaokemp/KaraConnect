package net.bramp.ffmpeg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.bramp.commons.lang3.math.gson.FractionAdapter;
import net.bramp.ffmpeg.gson.LowercaseEnumTypeAdapterFactory;
import net.bramp.ffmpeg.gson.NamedBitsetAdapter;
import net.bramp.ffmpeg.probe.FFmpegDisposition;
import org.apache.commons.lang3.math.Fraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Helper class with commonly used methods
 */
public final class FFmpegUtils {

  final static Gson gson = FFmpegUtils.setupGson();
  final static Pattern BITRATE_REGEX = Pattern.compile("(\\d+(?:\\.\\d+)?)kbits/s");


  FFmpegUtils() {
    throw new AssertionError("No instances for you!");
  }

  /**
   * Convert milliseconds to "hh:mm:ss.ms" String representation.
   *
   * @param milliseconds time duration in milliseconds
   * @return time duration in human-readable format
   */
  public static String millisecondsToString(long milliseconds) {
    // FIXME Negative durations are also supported.
    // https://www.ffmpeg.org/ffmpeg-utils.html#Time-duration
    checkArgument(milliseconds >= 0, "milliseconds must be positive");

    long seconds = milliseconds / 1000;
    milliseconds = milliseconds - (seconds * 1000);

    long minutes = seconds / 60;
    seconds = seconds - (minutes * 60);

    long hours = minutes / 60;
    minutes = minutes - (hours * 60);

    if (milliseconds == 0)
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);

    return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds);
  }

  /**
   * Converts a string representation of bitrate to a long of bits per second
   *
   * @param bitrate in the form of 12.3kbits/s
   * @return the bitrate in bits per second.
   */
  public static long parseBitrate(String bitrate) {
    Matcher m = BITRATE_REGEX.matcher(bitrate);
    if (!m.find()) {
      throw new IllegalArgumentException("Invalid bitrate '" + bitrate + "'");
    }

    return (long) (Float.parseFloat(m.group(1)) * 1000);
  }

  static Gson getGson() {
    return gson;
  }

  private static Gson setupGson() {
    GsonBuilder builder = new GsonBuilder();

    builder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory());
    builder.registerTypeAdapter(Fraction.class, new FractionAdapter());
    builder.registerTypeAdapter(FFmpegDisposition.class, new NamedBitsetAdapter<>(
        FFmpegDisposition.class));

    return builder.create();
  }
}
