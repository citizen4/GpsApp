package c4.gpsapp;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;

import android.view.MotionEvent;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.graphics.AndroidResourceBitmap;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.rendertheme.InternalRenderTheme;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class MapActivity extends Activity implements LocationListener, GpsStatus.NmeaListener
{
   // name of the map file in the external storage
   private static final String MAPFILE = "berlin.map";

   private LocationManager mLocationManager;
   private Marker mCurrentPosMarker;
   private LatLong mStartPosition = new LatLong(52.50, 13.29);
   private MapView mapView;
   private TileCache tileCache;
   private TileRendererLayer tileRendererLayer;


   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      AndroidGraphicFactory.createInstance(this.getApplication());

      mapView = new MapView(this);

      setContentView(this.mapView);

      mapView.setClickable(true);
      mapView.getMapScaleBar().setVisible(true);
      mapView.setBuiltInZoomControls(true);
      mapView.getMapZoomControls().setZoomLevelMin((byte) 10);
      mapView.getMapZoomControls().setZoomLevelMax((byte) 20);

      // create a tile cache of suitable size
      tileCache = AndroidUtil.createTileCache(this, "mapcache",
            mapView.getModel().displayModel.getTileSize(), 1f,
            this.mapView.getModel().frameBufferModel.getOverdrawFactor());

      Drawable marker = getResources().getDrawable(R.drawable.circle_blue);

      mCurrentPosMarker = new Marker(mStartPosition,AndroidGraphicFactory.convertToBitmap(marker),0,0);

      mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

      if(mLocationManager != null){
         mLocationManager.addNmeaListener(this);
      }
   }

   @Override
   protected void onStart()
   {
      super.onStart();

      mapView.getModel().mapViewPosition.setCenter(mStartPosition);
      mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);

      mCurrentPosMarker.setLatLong(mStartPosition);

      // tile renderer layer using internal render theme
      tileRendererLayer = new TileRendererLayer(tileCache, mapView.getModel().mapViewPosition, false, false, AndroidGraphicFactory.INSTANCE);
      tileRendererLayer.setMapFile(getMapFile());
      tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

      // only once a layer is associated with a mapView the rendering starts
      mapView.getLayerManager().getLayers().add(tileRendererLayer);
      mapView.getLayerManager().getLayers().add(mCurrentPosMarker);

      if(mLocationManager != null){
         mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this);
      }

   }

   @Override
   protected void onStop()
   {
      super.onStop();

      if(mLocationManager != null){
         mLocationManager.removeUpdates(this);
      }
      
      mapView.getLayerManager().getLayers().remove(mCurrentPosMarker);
      mapView.getLayerManager().getLayers().remove(tileRendererLayer);
      tileRendererLayer.onDestroy();
   }

   @Override
   protected void onDestroy()
   {
      this.tileCache.destroy();
      this.mapView.getModel().mapViewPosition.destroy();
      this.mapView.destroy();

      AndroidResourceBitmap.clearResourceBitmaps();

      super.onDestroy();
   }


   @Override
   public void onNmeaReceived(long timestamp, String nmea)
   {
      Gnss.parseNmeaSentence(nmea);
      if(Gnss.gpsState.hasFix){
         LatLong currentPosition = new LatLong(Gnss.gpsData.latitude,Gnss.gpsData.longitude);
         mCurrentPosMarker.setLatLong(currentPosition);
         mapView.getModel().mapViewPosition.setCenter(currentPosition);
         mapView.invalidate();
      }
   }

   private File getMapFile()
   {
      File file = new File(Environment.getExternalStorageDirectory(), "Maps/" + MAPFILE);
      return file;
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
