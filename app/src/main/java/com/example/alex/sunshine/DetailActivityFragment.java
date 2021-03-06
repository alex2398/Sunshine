package com.example.alex.sunshine;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toolbar;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public static String TAG = MainActivity.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    String mForecast = null;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);


        /* Obtenemos los datos del intent de la actividad y los asignamos al textView del fragment */
        Intent intent = getActivity().getIntent();
        // Si el intent lleva datos y ese datos es el que hemos etiquetado como mForecast, lo mostramos
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
            TextView forecastTextView = (TextView) rootview.findViewById(R.id.detailTextView);
            forecastTextView.setText(mForecast);
        }


        return rootview;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Asignamos el ShareActionProvider al menuItem "compartir"
        ShareActionProvider mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();

        // Le asignamos un nuevo intent de compartir
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(TAG, "Share action provider is null?");
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // Click en settings, abrimos settings
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        } else {
            // Click en icono share, creamos el intent compartir
            if (id == R.id.action_share) {
                createShareForecastIntent();
                return true;

            }
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createShareForecastIntent() {
        // Creamos este método para llamarlo y crear el intent para compartir
        Intent intent = new Intent(Intent.ACTION_SEND);
        // Organizamos el stack para que al volver de la app elegida, retorne a nuestra app
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return intent;
    }


}
