package ca.shane_brown.timhortonsroulette;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.IconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MapActivity extends AppCompatActivity {

    MapView map;
    MyLocationNewOverlay locOverlay;
    RoadManager rm;
    int radius = 0;
    String coords;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.settings){
            Intent coordinates = new Intent(MapActivity.this, Coordinates.class);
            coordinates.putExtra("coords", coords);
            startActivity(coordinates);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle args = getIntent().getExtras();

        if(args != null){
            radius = args.getInt("radius");
        }

        map = findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        locOverlay = new MyLocationNewOverlay(map);
        map.getOverlays().add(locOverlay);
        locOverlay.enableMyLocation();
        locOverlay.enableFollowLocation();

        map.getController().animateTo(locOverlay.getMyLocation());
        map.getController().setZoom(11.0);

        rm = new OSRMRoadManager(this);

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location loc;
        try {
            loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }catch(NullPointerException ex){
            Log.e("ROULETTE", "Failed to get last known location");
            return;
        }catch(SecurityException ex){
            Log.e("ROULETTE", "Failed to get permission for GPS position");
            return;
        }

        ArrayList<Double> coords = new ArrayList<Double>();
        coords.add(loc.getLatitude());
        coords.add(loc.getLongitude());

        Toast.makeText(getApplicationContext(), "Searching...", Toast.LENGTH_LONG).show();
        new TimsLocations().execute(coords);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(map != null)
            map.onPause();

        locOverlay.disableMyLocation();
        locOverlay.disableFollowLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(map != null)
            map.onResume();

        locOverlay.enableFollowLocation();
        locOverlay.enableMyLocation();
    }

    private class TimsLocations extends AsyncTask<ArrayList<Double>, JSONObject, GeoPoint>{
        @Override
        protected GeoPoint doInBackground(ArrayList<Double>... pos) {
            String base = "http://overpass-api.de/api/interpreter/?data=";
            String data = "[out:json][timeout:25];(node[\"name\"=\"Tim Hortons\"](around:" + radius + "," + pos[0].get(0) + "," + pos[0].get(1) + "););out body;>;out skel qt;";

            URL link;
            try{
                link = new URL(base + URLEncoder.encode(data));
            }catch(MalformedURLException ex){
                ex.printStackTrace(System.err);
                return null;
            }

            try {
                HttpURLConnection conn = (HttpURLConnection) link.openConnection();

                conn.setRequestMethod("GET");
                int response = conn.getResponseCode();

                if(response == HttpsURLConnection.HTTP_OK){
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String line;
                    StringBuffer respBodyBuf = new StringBuffer();

                    while((line = br.readLine()) != null){
                        respBodyBuf.append(line);
                    }
                    br.close();

                    String restBody = respBodyBuf.toString();

                    JSONObject root = new JSONObject(restBody);

                    JSONArray elements = root.getJSONArray("elements");

                    int roulette = (int)(Math.random() * elements.length());

                    Log.v("ROULETTE", "elements len: " + elements.length() + ", roulette: " + roulette);

                    JSONObject curTims = (JSONObject)elements.get(roulette);

                    GeoPoint point = new GeoPoint(curTims.getDouble("lat"), curTims.getDouble("lon"));

                    coords = point.getLatitude() + "," + point.getLongitude();

                    Log.i("ROULETTE", point.getLatitude() + "," + point.getLongitude());

                    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
                    waypoints.add(new GeoPoint(pos[0].get(0), pos[0].get(1)));
                    waypoints.add(point);

                    Road road = rm.getRoad(waypoints);
                    Polyline roadOverlay = rm.buildRoadOverlay(road);
                    map.getOverlays().add(roadOverlay);

                    Marker mark = new Marker(map);
                    mark.setPosition(point);
                    map.getOverlays().add(mark);

                    GeocoderNominatim coder = new GeocoderNominatim("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");
                    List<Address> adr = coder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                    mark.setTitle(adr.get(0).getAddressLine(0));

                    return point;
                }
            }catch(Exception ex){
                ex.printStackTrace(System.err);
            }

            return null;
        }

        @Override
        protected void onPostExecute(GeoPoint point) {
            super.onPostExecute(point);

            if(point != null) {
                Toast.makeText(getApplicationContext(), "Done.", Toast.LENGTH_SHORT).show();
                map.getController().animateTo(point);
                map.invalidate();
            }else{
                Toast.makeText(getApplicationContext(), "Point not found.", Toast.LENGTH_SHORT).show();
            }
        }


    }
}
