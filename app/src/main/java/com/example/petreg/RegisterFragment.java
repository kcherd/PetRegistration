package com.example.petreg;

import android.annotation.SuppressLint;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegisterFragment extends Fragment {

    public static final String TAG = RegisterFragment.class.getSimpleName();
    private TextView idTV;
    private TextView labelId;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.register_fragment, container, false);
        idTV = v.findViewById(R.id.id_reg_et);
        labelId = v.findViewById(R.id.textView1);
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
                idTV.setVisibility(View.VISIBLE);
                labelId.setVisibility(View.VISIBLE);
                writeToDB();
            }
        });

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (MainActivity)context;
        listener.onDialogDisplayed();
        ((MainActivity) context).setIsWrite(true);
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
                if(idTV.getText()!=null){
                    String toRecordMess = idTV.getText().toString();
                    mimeRecord = NdefRecord.createMime("text/plain", toRecordMess.getBytes(Charset.forName("US-ASCII")));
                } else{
                    mimeRecord = NdefRecord.createMime("text/plain", message.getBytes(Charset.forName("US-ASCII")));
                }
                ndef.writeNdefMessage(new NdefMessage(mimeRecord));
                ndef.close();
                //Write Successful
                Toast.makeText(getActivity(), getString(R.string.message_write_success), Toast.LENGTH_LONG).show();

            } catch (IOException | FormatException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), getString(R.string.repeat_write), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void writeToDB(){
        Pet pet = new Pet();
        boolean correctData = true;
        StringBuilder errMessage = new StringBuilder();
        if(!nameET.getText().toString().equals("")){
            pet.setName(nameET.getText().toString());
        } else{
            correctData = false;
            errMessage.append(getString(R.string.not_null_name));
            errMessage.append("\n");
        }
        //?????? ???????????? 0 ?? ???????????? ?????? ?????????? ???????????????? ????????
        if(!ageET.getText().toString().equals("") && Integer.parseInt(ageET.getText().toString()) > 0 && Integer.parseInt(ageET.getText().toString()) <= Calendar.getInstance().get(Calendar.YEAR)){
            pet.setBirth(Integer.parseInt(ageET.getText().toString()));
        } else{
            correctData = false;
            errMessage.append(getString(R.string.wrong_birth));
            errMessage.append("\n");
        }
        if(!fioET.getText().toString().equals("")){
            pet.setFio(fioET.getText().toString());
        } else{
            correctData = false;
            errMessage.append(getString(R.string.not_null_fio));
            errMessage.append("\n");
        }
        if(!addressET.getText().toString().equals("")){
            pet.setAddress(addressET.getText().toString());
        } else{
            correctData = false;
            errMessage.append(getString(R.string.not_null_address));
            errMessage.append("\n");
        }
        if(!telET.getText().toString().equals("")){
            pet.setTel(telET.getText().toString());
        } else{
            correctData = false;
            errMessage.append(getString(R.string.not_null_tel));
            errMessage.append("\n");
        }

        if (!correctData){
            Toast.makeText(getActivity(), errMessage.toString(),Toast.LENGTH_LONG).show();
        } else {
            pet.setTokenFCM(PetFirebaseMessagingService.token);
            Log.d(TAG, "object to write: " + pet.toString());

            ApiUtils.getApi().insertPet(gson.toJson(pet)).enqueue(
                    new Callback<JsonPrimitive>() {
                        //???????????????????? Handler, ?????????? ???????????????????? ???????????? ?? Main ????????????, ??.??. ???????? ???????????????? ???????????????????????? ?? ?????????????? ????????????
                        Handler mainHandler = new Handler(getActivity().getMainLooper());
                        @Override
                        public void onResponse(Call<JsonPrimitive> call, final Response<JsonPrimitive> response) {
                            mainHandler.post(new Runnable() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void run() {
                                    long idPet = response.body().getAsLong();
                                    Log.d(TAG, "response.body: " + idPet);
                                    idTV.setText(Long.toString(idPet));
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<JsonPrimitive> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t.getMessage());
                            Toast.makeText(getActivity(), getString(R.string.not_write_to_db) + t.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }
}