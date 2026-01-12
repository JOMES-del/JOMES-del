package com.example.weatherapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherApp";
    private EditText etCityName;
    private TextView tvCity, tvTemp, tvCondition, tvHumidity, tvWindSpeed;
    private ImageView ivWeatherIcon;
    private ProgressBar progressBar;

    // TODO: Sign up at https://openweathermap.org/ to get your free API key
    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle window insets for edge-to-edge
        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        etCityName = findViewById(R.id.etCityName);
        Button btnGetWeather = findViewById(R.id.btnGetWeather);
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvCondition = findViewById(R.id.tvCondition);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        progressBar = findViewById(R.id.progressBar);

        if (btnGetWeather != null) {
            btnGetWeather.setOnClickListener(v -> {
                String city = etCityName.getText().toString().trim();
                if (!city.isEmpty()) {
                    hideKeyboard();
                    getWeatherData(city);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.enter_city_prompt), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Handle "Enter" key on keyboard
        if (etCityName != null) {
            etCityName.setOnEditorActionListener((v, actionId, event) -> {
                String city = etCityName.getText().toString().trim();
                if (!city.isEmpty()) {
                    hideKeyboard();
                    getWeatherData(city);
                    return true;
                }
                return false;
            });
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void getWeatherData(String city) {
        String url = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    try {
                        // Main weather info
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        int humidity = main.getInt("humidity");

                        // Wind info
                        JSONObject wind = response.getJSONObject("wind");
                        double speed = wind.getDouble("speed");

                        // Condition and Icon
                        JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                        String description = weather.getString("description");
                        String iconCode = weather.getString("icon");
                        String cityName = response.getString("name");

                        // Update UI
                        if (tvCity != null) tvCity.setText(cityName);
                        if (tvTemp != null) tvTemp.setText(String.format(Locale.getDefault(), "%.1f °C", temp));
                        if (tvCondition != null) tvCondition.setText(description);
                        if (tvHumidity != null) tvHumidity.setText(String.format(Locale.getDefault(), "Humidity: %d%%", humidity));
                        if (tvWindSpeed != null) tvWindSpeed.setText(String.format(Locale.getDefault(), "Wind: %.1f km/h", speed));

                        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                        if (ivWeatherIcon != null) {
                            Glide.with(MainActivity.this).load(iconUrl).into(ivWeatherIcon);
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Parsing error", e);
                        Toast.makeText(MainActivity.this, getString(R.string.parse_error), Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Volley error", error);
                    boolean isDefaultKey = "YOUR_API_KEY_HERE".equals(API_KEY);
                    if (isDefaultKey) {
                        if (tvCondition != null) tvCondition.setText(R.string.missing_api_key);
                        Toast.makeText(MainActivity.this, R.string.set_api_key_toast, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.city_not_found, Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(jsonObjectRequest);
    }
}
