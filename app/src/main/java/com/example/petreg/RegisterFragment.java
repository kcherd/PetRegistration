package com.example.petreg;

import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.Charset;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegisterFragment extends Fragment {

    public static final String TAG = RegisterFragment.class.getSimpleName();
    private EditText idET;
    private EditText nameET;
    private EditText ageET;
    private EditText fioET;
    private EditText addressET;
    private EditText telET;
    private Button recordButton;

    private Listener listener;
    private Gson gson = new Gson();

    public static RegisterFragment newInstance(){
        return new RegisterFragment();
    }

//    public interface RegisterEventListener {
//        public void registerEvent(String id);
//    }
//
//    RegisterEventListener eventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.register_fragment, container, false);
        idET = v.findViewById(R.id.id_reg_et);
        nameET = v.findViewById(R.id.name_reg_et);
        ageET = v.findViewById(R.id.age_reg_et);
        fioET = v.findViewById(R.id.fio_reg_et);
        addressET = v.findViewById(R.id.address_reg_et);
        telET = v.findViewById(R.id.tel_reg_et);
        recordButton = v.findViewById(R.id.record_button);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                writeToDB();
            }
        });

//        recordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                eventListener.registerEvent(idTV.getText().toString());
//            }
//        });
        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (MainActivity)context;
        listener.onDialogDisplayed();
        ((MainActivity) context).setIsWrite(true);
        //eventListener = (RegisterEventListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener.onDialogDismissed();
    }

    public void onNfcDetected(Ndef ndef, String messageToWrite){
        writeToNfc(ndef, messageToWrite);
    }

    private void writeToNfc(Ndef ndef, String message){
        if (ndef != null) {
            try {
                ndef.connect();
                NdefRecord mimeRecord;
                if(idET.getText()!=null){
                    String toRecordMess = idET.getText().toString();
                    mimeRecord = NdefRecord.createMime("text/plain", toRecordMess.getBytes(Charset.forName("US-ASCII")));
                } else{
                    mimeRecord = NdefRecord.createMime("text/plain", message.getBytes(Charset.forName("US-ASCII")));
                }
                ndef.writeNdefMessage(new NdefMessage(mimeRecord));
                ndef.close();
                //Write Successful
                Toast.makeText(getActivity(), "Запись прошла успешно!", Toast.LENGTH_LONG).show();

            } catch (IOException | FormatException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Пожалуйста, попробуйте произвести записть еще раз", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void writeToDB(){
        //сделать проверку на пустые поля
        Pet pet = new Pet();
        pet.setName(nameET.getText().toString());
        pet.setAge(Integer.parseInt(ageET.getText().toString()));
        pet.setFio(fioET.getText().toString());
        pet.setAddress(addressET.getText().toString());
        pet.setTel(telET.getText().toString());

        Log.d(TAG, "object to write: " + pet.toString());

        ApiUtils.getApi().insertPet(gson.toJson(pet)).enqueue(
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

                                long idPet = gson.fromJson(jsonAns.getAsJsonObject(), Long.class);
                                idET.setText((int) idPet);
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
}
