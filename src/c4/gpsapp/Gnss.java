package c4.gpsapp;


import android.util.Log;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class Gnss
{
   private static final String LOG_TAG = "Gnss_NMEA";
   private static final SimpleDateFormat TimeFormat;

   public static Gnss.Data gpsData;
   public static Gnss.State gpsState;

   static {
      TimeFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
      TimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      gpsData = new Gnss.Data();
      gpsState = new Gnss.State();
   }


   private Gnss()
   {
   }

   public static void parseNmeaSentence(final String sentence)
   {
      String talkerId;
      String sentenceId;
      String[] words = sentence.split(",");

      Log.d(LOG_TAG,sentence);

      if (words[0].length() != 6) {
         throw new RuntimeException("Wrong start word of NMEA sentence");
      }

      if (!validateChecksum(sentence)) {
         Log.d(LOG_TAG, "Checksum error: '" + sentence + "'");
         return;
      }


      talkerId = words[0].substring(1, 3);
      sentenceId = words[0].substring(3);

      try {
         switch (sentenceId) {
            case "GSV":
               parseGSV(words);
               break;
            case "GSA":
               parseGSA(words);
               break;
            case "VTG":
               parseVTG(words);
               break;
            case "RMC":
               parseRMC(words);
               break;
            case "GGA":
               parseGGA(words);
               break;
            default:
               Log.e(LOG_TAG, "Unsupported sentence type: '" + sentenceId + "'");
               break;
         }
      } catch (Exception e) {
         Log.e(LOG_TAG, "NMEA sentence parse error!");
         e.printStackTrace();
      }

      //dataSet.setGnss(gpsData);

   }

   /*
      -- GPS satellites in view --
      e.g. $GLGSV,2,1,08,66,41,316,13,76,28,212,36,75,83,161,17,65,78,167,24*61
   */
   private static void parseGSV(final String[] words)
   {

   }

   /*
      -- GPS DOP and active satellites --
      e.g. $GPGSA,A,3,02,03,05,07,08,10,19,26,28,30,,,1.9,1.7,0.9*3A
   */
   private static void parseGSA(final String[] words)
   {
      int numOfSats = 0;

      for(int i = 0; i < 12; i++){
         numOfSats += (words[3+i].isEmpty() ? 0 : 1);
      }

      gpsState.satsInView = numOfSats;
   }

   /*
      -- Course over ground and ground speed --
      e.g. $GPVTG,,T,,M,0.0,N,0.0,K,D*26
   */
   private static void parseVTG(final String[] words)
   {
      gpsData.course = words[1].isEmpty() ? 0.0f : Float.parseFloat(words[1]);
      gpsData.speedKmh = words[7].isEmpty() ? 0.0f : Float.parseFloat(words[7]);
   }

   /*
      -- Recommended minimum specific GPS data --
      e.g. $GPRMC,033135,A,5133.658804,N,01429.682174,E,0.0,,250714,0.0,E,D*36
   */
   private static void parseRMC(final String[] words) throws ParseException
   {
      if (words[2].equals("V")) {
         gpsState.hasFix = false;
         return;
      } else {
         gpsState.hasFix = true;
      }

      gpsData.latitude = latitude2Decimal(words[3], words[4]);
      gpsData.longitude = longitude2Decimal(words[5], words[6]);
      parseTimeAndDate(words[1], words[9]);
   }

   /*
      -- GPS fix data --
      $GPGGA,033134,5133.658804,N,01429.682175,E,2,10,1.7,33.0,M,43.0,M,,*72
   */
   private static void parseGGA(final String[] words)
   {
      if (words[6].equals("0")) {
         gpsState.hasFix = false;
         return;
      } else {
         gpsState.hasFix = true;
      }

      gpsData.latitude = latitude2Decimal(words[2], words[3]);
      gpsData.longitude = longitude2Decimal(words[4], words[5]);
      gpsData.altitude = words[9].isEmpty() ? 0.0f : Float.parseFloat(words[9]);
      gpsState.satsInView = Integer.parseInt(words[7]);
      //parseTimeAndDate(words[1],null);
   }


   private static boolean validateChecksum(final String sentence)
   {
      int i;
      int checkSum;
      int xorSum = 0;

      for (i = 1; i < sentence.length(); i++) {
         char c = sentence.charAt(i);

         if (c == '*')
            break;

         xorSum ^= (byte) c;
      }

      try {
         //this may fail...
         checkSum = Integer.parseInt(sentence.substring(i + 1, i + 3), 16);
      } catch (RuntimeException e) {
         e.printStackTrace();
         return false;
      }

      //Log.d(LOG_TAG,String.format("xorSum: %02X checkSum: %02X", xorSum, checkSum));
      return (xorSum - checkSum) == 0;
   }

   private static float latitude2Decimal(final String lat, final String northOrSouth)
   {
      float degrees = Float.parseFloat(lat.substring(0, 2));
      degrees += Float.parseFloat(lat.substring(2)) / 60.0f;

      return northOrSouth.startsWith("S") ? -degrees : degrees;
   }

   private static float longitude2Decimal(final String lng, final String westOrEast)
   {
      float degrees = Float.parseFloat(lng.substring(0, 3));
      degrees += Float.parseFloat(lng.substring(3)) / 60.0f;

      return westOrEast.startsWith("W") ? -degrees : degrees;
   }

   private static void parseTimeAndDate(final String time, final String date) throws ParseException
   {
      int day = Integer.parseInt(date.substring(0, 2));
      int month = Integer.parseInt(date.substring(2, 4));
      int year = Integer.parseInt(date.substring(4));
      int hours = Integer.parseInt(time.substring(0, 2));
      int minutes = Integer.parseInt(time.substring(2, 4));
      int seconds = Integer.parseInt(time.substring(4));

      gpsData.utcTime = TimeFormat.parse(String.format("%02d.%02d.%02d %02d:%02d:%02d",
            day, month, year, hours, minutes, seconds));
   }


   public static class Data
   {
      Date utcTime = null;
      float latitude = 0.0f;
      float longitude = 0.0f;
      float altitude = 0.0f;
      float speedKmh = 0.0f;
      float course = 0.0f;
   }

   public static class State
   {
      boolean hasFix = false;
      int satsInView = 0;
   }

}
