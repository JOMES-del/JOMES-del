package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etCityName;
    private Button btnGetWeather;
    private TextView tvCity, tvTemp, tvCondition;
    private ImageView ivWeatherIcon;

    // TODO: Sign up at https://openweathermap.org/ to get your free API key
    private final String API_KEY = "YOUR_API_KEY_HERE";
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etCityName = findViewById(R.id.etCityName);
        btnGetWeather = findViewById(R.id.btnGetWeather);
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvCondition = findViewById(R.id.tvCondition);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        btnGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etCityName.getText().toString().trim();
                if (!city.isEmpty()) {
                    getWeatherData(city);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getWeatherData(String city) {
        String url = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Get Main Object (Temperature)
                            JSONObject main = response.getJSONObject("main");
                            double temp = main.getDouble("temp");

                            // Get Weather Array (Condition and Icon)
                            JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                            String description = weather.getString("description");
                            String iconCode = weather.getString("icon");
                            String cityName = response.getString("name");

                            // Update UI
                            tvCity.setText(cityName);
                            tvTemp.setText(String.format(Locale.getDefault(), "%.1f °C", temp));
                            tvCondition.setText(description);

                            // Load Weather Icon using Glide
                            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                            Glide.with(MainActivity.this).load(iconUrl).into(ivWeatherIcon);

                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (API_KEY.equals("YOUR_API_KEY_HERE")) {
                    tvCondition.setText("Missing API Key");
                    Toast.makeText(MainActivity.this, "Please set your API key in MainActivity.java", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "City not found or connection error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        queue.add(jsonObjectRequest);
    }
}
