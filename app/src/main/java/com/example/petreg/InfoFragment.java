package com.example.petreg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

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
    private Pet pet;
    public static final String TAG = InfoFragment.class.getSimpleName();

    public static InfoFragment newInstance(){
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

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (MainActivity)context;
        listener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener.onDialogDismissed();
    }

    public void onNfcDetected(Ndef ndef){
        readFromNfc(ndef);
    }

    private void readFromNfc(Ndef ndef){
        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            if(ndefMessage == null){
                Toast.makeText(getActivity(), "Записей на метке нет!", Toast.LENGTH_LONG).show();
            }
            else {
                String message = new String(ndefMessage.getRecords()[0].getPayload());
                Log.d(TAG, "readFromNFC: " + message);
                idTV.setText(message);
                ndef.close();

                getDataFromDB();
            }

        } catch (IOException | FormatException e) {
            e.printStackTrace();

        }
    }

    public void getDataFromDB(){
        ApiUtils.getApi().getPet(1).enqueue(
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

                                pet = new Pet();
                                pet = gson.fromJson(jsonAns.getAsJsonObject(), Pet.class);
                                setPetInfo();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                    }
                }
        );
    }

    @SuppressLint("SetTextI18n")
    public void setPetInfo(){
        nameTV.setText(pet.getName());
        ageTV.setText(pet.getAge() + "");
        fioTV.setText(pet.getFio());
        addressTV.setText(pet.getAddress());
        telTV.setText(pet.getTel());
    }
}
