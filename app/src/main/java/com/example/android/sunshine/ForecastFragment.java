package com.example.android.sunshine;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private final String LOG_CAT = ForecastFragment.class.getSimpleName();
    private ArrayAdapter<String> adaptador;
    EditText etCp;
    SharedPreferences appPrefs;
    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adaptador = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

       // etCp = (EditText) rootView.findViewById(R.id.et_cp);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adaptador);

       // String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
        //String apiKey = "090452f40f25cac25820b3ab4882017b";
        /*String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
        URL url = new URL(baseUrl.concat(apiKey));
        new FetchWeatherTask().execute();*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = adaptador.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                Log.d(LOG_CAT, "----> "+ forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            updateWeather();
            return true;
        }
        if(id==R.id.action_map){
            viewLocationOnMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewLocationOnMap(){
        String location = appPrefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
       /* Uri geoLocation = Uri.parse("geo:"+location);*/
        Uri geoLocation = Uri.parse("geo:0,0?")
                .buildUpon()
                .appendQueryParameter("q", location)
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        Log.v(LOG_CAT, "----> geoLocation: " + geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            Log.v(LOG_CAT, "----> getActivity().getPackageManager(): " + getActivity().getPackageManager());
            startActivity(intent);
        }
    }
    private void updateWeather(){
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        /*SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());*/
        appPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String cpStr = appPrefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        //weatherTask.execute("94043");
        weatherTask.execute(cpStr);
        /*weatherTask.execute(params);*/
    }

    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        *//*
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }*/

    /**
     * Prepare the weather high/lows for presentation.
     *//*
    private String formatHighLows(double high, double low) {

        String unitTemp = appPrefs.getString(
                getString(R.string.pref_tempunit_key),
                getString(R.string.pref_tempunit_default));
        if(unitTemp.equals(R.string.pref_tempunit_imperial)){
            high = 32 + 1.8*high;
            low  = 32 + 1.8*low;
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }*/

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     *//*
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
 //       for(int i = 0; i < weatherArray.length(); i++) {
        for(int i = 0; i<numDays; i++) {
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

            *//*String unitTemp = appPrefs.getString(
                    getString(R.string.pref_tempunit_key),
                    getString(R.string.pref_tempunit_default));
            if(unitTemp.equals("IMP")){
                high = 32 + 1.8*high;
                low  = 32 + 1.8*low;
            }*//*

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrs;

    }
*/
    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_CAT = FetchWeatherTask.class.getSimpleName();

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {

            String unitTemp = appPrefs.getString(
                    getString(R.string.pref_tempunit_key),
                    getString(R.string.pref_tempunit_metric));
            Log.v(LOG_CAT, "----> unitTemp: " + unitTemp);
            Log.v(LOG_CAT, "----> R.string.pref_tempunit_imperial: " + getString(R.string.pref_tempunit_imperial));

            if(unitTemp.equals(getString(R.string.pref_tempunit_imperial))){
                high = 32 + 1.8*high;
                low  = 32 + 1.8*low;
            }

            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

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
         * Take the String representing the complete forecast in JSON Format and
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
            //       for(int i = 0; i < weatherArray.length(); i++) {
            for(int i = 0; i<numDays; i++) {
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

            /*String unitTemp = appPrefs.getString(
                    getString(R.string.pref_tempunit_key),
                    getString(R.string.pref_tempunit_default));
            if(unitTemp.equals("IMP")){
                high = 32 + 1.8*high;
                low  = 32 + 1.8*low;
            }*/

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;

        }


        @Override
        protected String[] doInBackground(String... params){

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            String mode ="json";
            String unit="metric";
            String dias = "7";

            try {
               /* String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL(baseUrl.concat(apiKey));*/

                //String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
                //String apiKey = "&APPID=" + "090452f40f25cac25820b3ab4882017b";
                final String CP = "q";
                final String MODE = "mode";
                final String UNIT = "units";
                final String NUM_DIAS = "cnt";

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter(CP, params[0])
                        .appendQueryParameter(MODE, mode)
                        .appendQueryParameter(UNIT, unit)
                        .appendQueryParameter(NUM_DIAS, dias);
                String baseUrl = builder.build().toString();

                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;

                URL url = new URL(baseUrl.concat(apiKey));

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                return getWeatherDataFromJson(forecastJsonStr, 7);

            }catch (IOException e){
                Log.e(LOG_CAT, "#### Error1 ", e);
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;

            }finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_CAT, "#### Error2 closing stream", e);
                    }
                }
            }
           /* try {
                return getWeatherDataFromJson(forecastJsonStr, 3);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        protected void onPostExecute(String[] result){
            adaptador.clear();
            for(String cadena:result){
                adaptador.add(cadena);
            }
            //adaptador.addAll(result); // a partir de ver 11

           /* double tempMax;
            try {
                tempMax = WeatherDataParser.getMaxTemperatureForDay(result,3);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }
    }
}
