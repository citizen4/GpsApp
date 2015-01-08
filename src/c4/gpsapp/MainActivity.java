package c4.gpsapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements LocationListener, GpsStatus.NmeaListener
{
   private static final String LOG_TAG = "";
   private SimpleDateFormat TimeFormat = new SimpleDateFormat("HH:mm:ss");
   private LocationManager mLocationManager;

   private List<TextView> mValueViewList;
   private TextView mSatsValueView;
   private TextView mTimeValueView;
   private TextView mLatValueView;
   private TextView mLngValueView;
   private TextView mAltValueView;
   private TextView mSpeedValueView;
   private TextView mCourseValueView;


   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      setup();
   }

   @Override
   protected void onResume()
   {
      Log.d(LOG_TAG, "onResume()");
      super.onResume();

      if(mLocationManager != null){
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this);
      }
   }

   @Override
   protected void onPause()
   {
      Log.d(LOG_TAG,"onPause()");
      super.onPause();

      if(mLocationManager != null){
         mLocationManager.removeUpdates(this);
      }

   }

   @Override
   protected void onStart()
   {
      Log.d(LOG_TAG,"onStart()");
      super.onStart();
   }

   @Override
   protected void onStop()
   {
      Log.d(LOG_TAG,"onStop()");
      super.onStop();
   }

   @Override
   protected void onDestroy()
   {
      Log.d(LOG_TAG,"onDestroy()");

      super.onDestroy();
   }

   @Override
   public void onNmeaReceived(long timestamp, String nmea)
   {
      Gnss.parseNmeaSentence(nmea);
      updateUI();
   }

   @Override
   public boolean onTouchEvent(MotionEvent event)
   {

      if(event.getAction() == MotionEvent.ACTION_UP){
         Intent mapActivityIntent = new Intent(this,MapActivity.class);
         startActivity(mapActivityIntent);
         return true;
      }

      return super.onTouchEvent(event);
   }

   private void setup()
   {
      mValueViewList = new ArrayList<>();

      mSatsValueView = (TextView) findViewById(R.id.sats_value);
      mTimeValueView = (TextView)findViewById(R.id.time_value);
      mLatValueView = (TextView)findViewById(R.id.lat_value);
      mLngValueView = (TextView)findViewById(R.id.lng_value);
      mAltValueView = (TextView)findViewById(R.id.alt_value);
      mSpeedValueView = (TextView)findViewById(R.id.speed_value);
      mCourseValueView = (TextView)findViewById(R.id.course_value);

      mValueViewList.add(mSatsValueView);
      mValueViewList.add(mTimeValueView);
      mValueViewList.add(mLatValueView);
      mValueViewList.add(mLngValueView);
      mValueViewList.add(mAltValueView);
      mValueViewList.add(mSpeedValueView);
      mValueViewList.add(mCourseValueView);

      mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

      if(mLocationManager != null){
         mLocationManager.addNmeaListener(this);
      }
   }

   private void updateUI()
   {
      mSatsValueView.setText(String.format("%d",Gnss.gpsState.satsInView));

      if(Gnss.gpsData.utcTime != null) {
         mTimeValueView.setText(TimeFormat.format(Gnss.gpsData.utcTime));
      }

      mLatValueView.setText(String.format("%.6f°",Gnss.gpsData.latitude));
      mLngValueView.setText(String.format("%.6f°",Gnss.gpsData.longitude));
      mAltValueView.setText(String.format("%.1f m",Gnss.gpsData.altitude));
      mSpeedValueView.setText(String.format("%.1f km/h",Gnss.gpsData.speedKmh));
      mCourseValueView.setText(String.format("%.1f°",Gnss.gpsData.course));

      for(TextView valueView: mValueViewList){
         valueView.setTextColor(Gnss.gpsState.hasFix ? Color.GREEN : Color.DKGRAY);
      }

   }

   @Override
   public void onLocationChanged(Location location) {}
   @Override
   public void onStatusChanged(String provider, int status, Bundle extras) {}
   @Override
   public void onProviderEnabled(String provider) {}
   @Override
   public void onProviderDisabled(String provider) {}
}
