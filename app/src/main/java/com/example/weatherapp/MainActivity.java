package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText etEnterCity;
    private Button btnFindResult;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEnterCity = (EditText) findViewById(R.id.et_enter_city);
        btnFindResult = (Button) findViewById(R.id.btn_find_result);
        tvResult = (TextView) findViewById(R.id.tv_result);

        // Обработка кнопки "Узнать погоду"
        // Если пользователь ничего не ввёл, то появлятся сообщение пользователю о вводе города
        // Если пользователь ввёл название города, то происходит формирование URL адреса и отправка за данными на сервер
        btnFindResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etEnterCity.getText().toString().trim().equals("")) {
                    Toast.makeText(getBaseContext(), R.string.no_user_input, Toast.LENGTH_LONG).show();
                } else {
                    String city = etEnterCity.getText().toString();
                    String key = "68b44ddd02afc52d41a1e49d46809440";
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";

                    new GetWeatherData().execute(url);
                }
            }
        });
    }

    // Класс для ассинхронного выполнения получения данных по API (openweatherapi)
    @SuppressLint("StaticFieldLeak")
    private class GetWeatherData extends AsyncTask<String, String, String> {
        // Перед выполнением запроса вывод текста об ожидании данных на экран
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvResult.setText("Ожидайте...");
        }

        // Отправка запроса и получение данных погоды с сервера
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        // Обработка полученного JSON результата и вывод на экран
        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject json = new JSONObject(result);

                String city = "Город: " + json.getString("name");
                String temp = "\nТемпература: " + Math.round(json.getJSONObject("main").getDouble("temp")) + " °C";
                String desc = "\nОписание: " + json.getJSONArray("weather").getJSONObject(0).getString("description");
                String humid = "\nВлажность воздуха: " + json.getJSONObject("main").getInt("humidity") + "%";
                String wind = "\nСкорость ветра: " + json.getJSONObject("wind").getInt("speed") + " м/с";

                tvResult.setText(city + desc + temp + humid + wind);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}