package com.example.petreg;

import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.nio.charset.Charset;


public class RegisterFragment extends Fragment {

    public static final String TAG = RegisterFragment.class.getSimpleName();
    private EditText idTV;
    private EditText nameTV;
    private Button recordButton;

    private Listener listener;

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
        idTV = v.findViewById(R.id.id_reg_tv);
        nameTV = v.findViewById(R.id.name_reg_tv);
        recordButton = v.findViewById(R.id.record_button);


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
                if(idTV.getText()!=null){
                    String toRecordMess = idTV.getText().toString();
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
}
