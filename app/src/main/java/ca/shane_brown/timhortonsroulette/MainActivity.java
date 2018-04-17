package ca.shane_brown.timhortonsroulette;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    Activity act;
    int radiusKm;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allGranted = true;
        if(requestCode == 0){
            for(int i = 0; i < grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    allGranted = false;
                }
            }
        }

        if(allGranted){
            Intent mapAct = new Intent(MainActivity.this, MapActivity.class);
            mapAct.putExtra("radius", radiusKm*1000);
            startActivity(mapAct);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.act = this;

        Button routeBtn = (Button)findViewById(R.id.routeBtn);
        final EditText radiusTxt = (EditText) findViewById(R.id.radiusTxt);
        radiusTxt.setSelection(radiusTxt.getText().length());
        routeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radiusKm = Integer.parseInt(radiusTxt.getText().toString());
                if(radiusKm > 0){
                    Intent mapAct = new Intent(MainActivity.this, MapActivity.class);
                    mapAct.putExtra("radius", radiusKm*1000);
                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(act, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

                    }else{
                        startActivity(mapAct);
                    }
                }
            }
        });


    }
}
