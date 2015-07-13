package com.example.alex.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A placeholder fragment containing a simple view.
 */

public class ForecastFragment extends Fragment{

    ListView forecast;
    // Constante para el log
    private String TAG = FetchWeatherTask.class.getSimpleName();
    private ArrayAdapter<String> adapter;


    public ForecastFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflamos la vista con el layout
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);

        forecast = (ListView) rootview.findViewById(R.id.listview_forecast);


        // Creamos datos falsos y rellenamos el adapter con ellos, luego los actualizaremos
        List<String> weekForecast = new ArrayList<>();

        /* El array adapter necesita los siguientes parametros:
            1. El context
            2. El layout que contiene la vista
            3. La vista dentro del layout que tiene que rellenar
            4. El array de datos que va a insertar
        */
        adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        forecast.setAdapter(adapter);

        forecast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String temperatureText = (String) forecast.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(),DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT,temperatureText);
                startActivity(intent);

                //Toast.makeText(getActivity(),temperatureText,Toast.LENGTH_LONG).show();
            }
        });

        return rootview;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        // Para obtener datos desde http creamos una clase
        // que extiende AsyncTask para no ejecutarla en el
        // MainThread, que solo esta destinado al UI
        // Las operaciones las haremos en el método doInBackground
        // NOTA: Esto es mas sencillo hacerlo con okhttp

        /* The date/time conversion code is going to be moved outside the asynctask later,
                * so for convenience we're breaking it out into its own method now.
                */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete mForecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

        @Override
        protected String[] doInBackground(String... params) {



            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            // Variables para la consulta http
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Uri.Builder builder = new Uri.Builder();

            // Declaramos la respuesta de cadena de JSON
            String forecastJsonStr = null;

            String format = "json";

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String units = preferences.getString("units",getString(R.string.pref_units_default));
            String language = "es";
            int numDays = 7;

            try {
            /* Construimos la URL de consulta para OpenWeatherMap */


                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String LANG_PARAM = "lang";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(LANG_PARAM, language)
                        .build();

                URL url = new URL(builtUri.toString());
                // Creamos el request a openweathermap y abrimos la conexion

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Leemos la cadena y la guardamos en un string

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Como es JSON, añdir nuevas lineas no es necesario, pero hace mas sencillo el debugging

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // La cadena esta vacia

                    return null;
                }

                forecastJsonStr = buffer.toString();
                Log.v(TAG,forecastJsonStr);


            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                // Si no obtenemos datos, no intentamos parsearlo
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);



            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Aqui solo se llega si hay un error
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            // Aqui se nos devuelve el resultado de doInBackground
            // que como hemos definido en la clase AsyncTask, es un array de strings

            if (strings != null) {
                // Limpiamos el adaptador (borrar)
                adapter.clear();
                // Añadimos los strings (actualizar)
                adapter.addAll(strings);

                // Otra forma:
                // mForecast.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Añadimos esta linea para indicar que el fragmento tiene opciones de menu
        // y para manejar los eventos en las mismas
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Con este metodo inflamos las opciones de menu basandonos en el xml
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Con este metodo manejamos los eventos en los items del menu


        int id = item.getItemId();
        if (id == R.id.refresh_data) {
            // Acciones del menu pulsado

            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void updateWeather() {
        String postalCode = null;
        String countryCode = null;
        // Obtenemos los valores de las preferencias de la app
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        postalCode = preferences.getString("location",getString(R.string.pref_location_default));
        countryCode = preferences.getString("country", getString(R.string.pref_country_default));

        // En este caso creamos una tarea para obtener el tiempo en segundo plano
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute(postalCode + "," + countryCode);
    }

}
