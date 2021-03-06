package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final int CAMERA_PERMISSION_CODE = 1;
    private final int CAMERA_REQUEST_CODE = 1;
    ImageView userImage;

    LocationManager locationManager;
    Location location;

    String URL = "https://api.openweathermap.org/data/2.5/weather";
    String CITY = "Caxias do Sul";
    String API_KEY = "aa7fb71f3997f591be484bdb58831bf6";
    String DEFAULT_FETCH_URL = URL + "?q=" + CITY + "&units=metric&appid=" + API_KEY;
    String FETCH_URL = DEFAULT_FETCH_URL;
    Bitmap userImg;

    TextView addressTxt,
            updated_atTxt,
            statusTxt,
            tempTxt,
            temp_minTxt,
            temp_maxTxt,
            sunriseTxt,
            sunsetTxt,
            windTxt,
            pressureTxt,
            humidityTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String searchTerm = getIntent().getStringExtra("searchTerm");
        if(!searchTerm.isEmpty())
            CITY = searchTerm;

        boolean myLocation = getIntent().getBooleanExtra("myLocation", false);

        mountURLToFetch();

        userImage = findViewById(R.id.userImage);
        userImage.setOnClickListener(this);
        Bitmap userImgRecorded = ((MyApplication) this.getApplication()).getUserImg();
        if(userImgRecorded != null)
            userImage.setImageBitmap(userImgRecorded);

        addressTxt = findViewById(R.id.address);
        updated_atTxt = findViewById(R.id.updated_at);
        statusTxt = findViewById(R.id.status);
        tempTxt = findViewById(R.id.temp);
        temp_minTxt = findViewById(R.id.temp_min);
        temp_maxTxt = findViewById(R.id.temp_max);
        sunriseTxt = findViewById(R.id.sunrise);
        sunsetTxt = findViewById(R.id.sunset);
        windTxt = findViewById(R.id.wind);
        pressureTxt = findViewById(R.id.pressure);
        humidityTxt = findViewById(R.id.humidity);

        if(myLocation){
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission cancelled! Accept it in the settings.", Toast.LENGTH_SHORT).show();
                location = null;
                FETCH_URL = DEFAULT_FETCH_URL;
                return;
            }else{
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                FETCH_URL = URL + "?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=" + API_KEY;
            }
        }

        new weatherTask().execute();
    }

    void mountURLToFetch(){
        DEFAULT_FETCH_URL = URL + "?q=" + CITY + "&units=metric&appid=" + API_KEY;
        FETCH_URL = DEFAULT_FETCH_URL;
    }

    @Override
    public void onClick(View view) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE);
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST_CODE);
            }else{
                Toast.makeText(this, "Camera permission cancelled! Accept it in the settings.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CAMERA_REQUEST_CODE){
                Log.d(data.toString(), data.toString());
                userImg = (Bitmap) data.getExtras().get("data");
                ((MyApplication) this.getApplication()).setUserImg(userImg);
                userImage.setImageBitmap(userImg);
            }
        }
    }

    class weatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Loading
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        protected String doInBackground(String... args) {
            String response = HttpRequest.Get(FETCH_URL);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {


            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                Long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                String temp = main.getString("temp") + "??C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "??C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "??C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");

                String address = jsonObj.getString("name") + ", " + sys.getString("country");

                addressTxt.setText(address);
                updated_atTxt.setText(updatedAtText);
                statusTxt.setText(weatherDescription.toUpperCase());
                tempTxt.setText(temp);
                temp_minTxt.setText(tempMin);
                temp_maxTxt.setText(tempMax);
                sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                windTxt.setText(windSpeed);
                pressureTxt.setText(pressure);
                humidityTxt.setText(humidity);

                // Finish loading
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);


            } catch (JSONException e) {
                // Error
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }

        }
    }
}