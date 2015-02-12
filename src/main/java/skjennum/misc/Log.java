package skjennum.misc;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


/**
 * Helper class for logging.
 */
public class Log {

  private static String getPrettyDate() {
    DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm:ss:SS");
    return format.print(new DateTime());
  }

  /**
   * Verbose logging. Visible in the standard output only
   */
  public static void v(String tag, Object msg, Exception e) {
    log(String.format("%s > %s > %s", getPrettyDate(), tag, msg));
    e.printStackTrace();
  }

  /**
   * Verbose logging. Visible in the standard output only
   */
  public static void v(String tag, Object msg) {
    log(String.format("%s > %s > %s", getPrettyDate(), tag, msg.toString()));
  }

  private static void log(String msg) {
    System.out.println(msg);
  }


}
