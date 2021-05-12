package com.example.petreg;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoFragment extends Fragment {
    private TextView idTV;
    private TextView nameTV;
    private TextView ageTV;
    private TextView fioTV;
    private TextView addressTV;
    private TextView telTV;

    private Listener listener;
    private Gson gson = new Gson();
    private Pet pet = new Pet();
    public static final String TAG = InfoFragment.class.getSimpleName();
    private static final int REQUEST_ACCESS_FINE_LOCATION_STATE = 100;
    private static final String ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private boolean permissionGPS = false;
    private LocationManager locationManager;
    private Coordinates currCoordinates = new Coordinates();


    private RecyclerView recyclerView;
    private VaccineAdapter adapter;

    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_fragment, container, false);
        idTV = v.findViewById(R.id.id_info_tv);
        nameTV = v.findViewById(R.id.name_info_tv);
        ageTV = v.findViewById(R.id.age_info_tv);
        fioTV = v.findViewById(R.id.fio_info_tv);
        addressTV = v.findViewById(R.id.address_info_tv);
        telTV = v.findViewById(R.id.tel_info_tv);

        recyclerView = v.findViewById(R.id.list);

        //проверка разрешения доступа к местоположению
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(ACCESS_FINE_LOCATION_PERMISSION, REQUEST_ACCESS_FINE_LOCATION_STATE);
        }
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100 * 10, 100, locationListener);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (MainActivity) context;
        listener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener.onDialogDismissed();
    }

    public void onNfcDetected(Ndef ndef) {
        readFromNfc(ndef);
    }

    private void readFromNfc(Ndef ndef) {
        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if (ndefMessage == null) {
                Toast.makeText(getActivity(), getString(R.string.no_record), Toast.LENGTH_LONG).show();
            } else {
                String message = new String(ndefMessage.getRecords()[0].getPayload());
                Log.d(TAG, "readFromNFC: " + message);
                idTV.setText(message);
                ndef.close();

                if (permissionGPS) {
                    if (!isGeoEnabled()) {
                        buildAlertMessageNoLocationService();
                    }

                    if (isGeoEnabled()) {
                        currCoordinates.setId(Long.parseLong(message));
                        sendCoordinatesToServer();
                        Toast.makeText(getActivity(), "lat = " + currCoordinates.getLatitude() + ", lon = " + currCoordinates.getLongitude(), Toast.LENGTH_LONG).show();
                    }
                }

                getDataFromDB(Long.parseLong(message));
            }

        } catch (IOException | FormatException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getString(R.string.err_read_tag),Toast.LENGTH_LONG).show();
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currCoordinates.setLatitude(location.getLatitude());
            currCoordinates.setLongitude(location.getLongitude());
            permissionGPS = true;
            Log.d(TAG, "onLocationChange, lat = " + currCoordinates.getLatitude() + ", lon = " + currCoordinates.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void sendCoordinatesToServer(){
        ApiUtils.getApi().setCoordinates(gson.toJson(currCoordinates)).enqueue(
                new Callback<JsonObject>() {
                    //используем Handler, чтобы показывать ошибки в Main потоке, т.к. наши коллбеки возвращаются в рабочем потоке
                    Handler mainHandler = new Handler(getActivity().getMainLooper());
                    @Override
                    public void onResponse(Call<JsonObject> call, final Response<JsonObject> response) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                JsonObject jsonAns = response.body();
                                Log.d(TAG, "response.body: " + jsonAns);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.d(TAG, "sendCoordinatesToServer onFailure: " + t);
                    }
                }
        );
    }

    public void getDataFromDB(long id){
        ApiUtils.getApi().getPet(id).enqueue(
                new Callback<JsonObject>() {
                    //используем Handler, чтобы показывать ошибки в Main потоке, т.к. наши коллбеки возвращаются в рабочем потоке
                    Handler mainHandler = new Handler(getActivity().getMainLooper());
                    @Override
                    public void onResponse(Call<JsonObject> call, final Response<JsonObject> response) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                JsonObject jsonAns = response.body();
                                Log.d(TAG, "run: " + jsonAns);

                                pet = gson.fromJson(jsonAns.getAsJsonObject(), Pet.class);
                                setPetInfo();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.d(TAG, "getDataFromDB onFailure: " + t.getMessage());
                        Toast.makeText(getActivity(), getString(R.string.not_read_from_db), Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @SuppressLint("SetTextI18n")
    public void setPetInfo(){
        nameTV.setText(pet.getName());
        int age = Calendar.getInstance().get(Calendar.YEAR) - pet.getBirth();
        ageTV.setText(age + "");
        fioTV.setText(pet.getFio());
        addressTV.setText(pet.getAddress());
        telTV.setText(pet.getTel());

        adapter = new VaccineAdapter(this.getContext(), pet.getVaccinations());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGPS = true;
                Toast.makeText(getActivity(), "Разрешения получены", Toast.LENGTH_LONG).show();
            } else {
                permissionGPS = false;
                Toast.makeText(getActivity(), "Разрешения не получены", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void requestPermission(String permission, int requestCode) {
        // запрашиваем разрешение
        ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
    }

    private boolean isGeoEnabled(){
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void buildAlertMessageNoLocationService() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setMessage("Включите GPS на устройстве")
                .setPositiveButton("Включить", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}