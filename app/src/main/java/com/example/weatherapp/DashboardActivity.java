package com.example.weatherapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button btnMyWeather = (Button) findViewById(R.id.btnMyWeather);
        Button btnSearchWeather = (Button) findViewById(R.id.btnSearchWeather);
        Button btnExit = (Button) findViewById(R.id.btnExit);

        btnMyWeather.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DashboardActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
                return;
            }
            navigateToWeatherMainScreen("", true);
        });

        btnSearchWeather.setOnClickListener(view -> showCustomDialog());

        btnExit.setOnClickListener(this::exitApp);
    }

    void showCustomDialog() {
        final Dialog dialog = new Dialog(DashboardActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_search_term);

        final EditText searchTerm = dialog.findViewById(R.id.searchTerm);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String searchTermString = searchTerm.getText().toString();
            navigateToWeatherMainScreen(searchTermString, false);
            dialog.dismiss();
        });

        dialog.show();
    }

    void navigateToWeatherMainScreen(String searchTermString, boolean myLocation){
        Intent main = new Intent(DashboardActivity.this, MainActivity.class);
        main.putExtra("searchTerm", searchTermString);
        main.putExtra("myLocation", myLocation);
        startActivity(main);
    }

    public void exitApp(View view) {
        finish();
        System.exit(0);
    }

}