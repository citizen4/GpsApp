package c4.gpsapp;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class GnssTest
{

   @Test
   public void foo()
   {
      try {
         Gnss.parseNmeaSentence("no nmea");
      }catch (RuntimeException e){
         return;
      }

      Assert.fail();
   }


}