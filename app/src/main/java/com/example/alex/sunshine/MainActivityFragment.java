package com.example.alex.sunshine;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */

public class MainActivityFragment extends Fragment{

    ListView forecast;
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflamos la vista con el layout
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);

        forecast = (ListView) rootview.findViewById(R.id.listview_forecast);


        ArrayList<String> weekForecast = new ArrayList<>();


        weekForecast.add("Hoy - Soleado - 22/40");
        weekForecast.add("Mañana - Soleado - 22/40");
        weekForecast.add("Lunes - Soleado - 22/40");
        weekForecast.add("Martes - Soleado - 22/40");
        weekForecast.add("Miercoles - Soleado - 22/40");

        /* El array adapter necesita los siguientes parametros:
            1. El context
            2. El layout que contiene la vista
            3. La vista dentro del layout que tiene que rellenar
            4. El array de datos que va a insertar
        */

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);


        forecast.setAdapter(adapter);

        return rootview;
    }


}
