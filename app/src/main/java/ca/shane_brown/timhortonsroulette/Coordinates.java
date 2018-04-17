package ca.shane_brown.timhortonsroulette;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Coordinates extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinates);

        EditText coordsTxt = (EditText) findViewById(R.id.coordsTxt);
        Button backBtn = (Button) findViewById(R.id.backBtn);

        Bundle args = getIntent().getExtras();
        coordsTxt.setText(args.getString("coords"));

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
