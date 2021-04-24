package com.example.petreg;

import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class InfoFragment extends Fragment {
    private TextView idTV;
    private TextView nameTV;
    private Listener listener;
    public static final String TAG = InfoFragment.class.getSimpleName();

    public static InfoFragment newInstance(){
        return new InfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_fragment, container, false);
        idTV = v.findViewById(R.id.id_info_tv);
//        if(getArguments() != null) {
//            idTV.setText(getArguments().getString("id"));
//        }

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
            }
            ndef.close();

        } catch (IOException | FormatException e) {
            e.printStackTrace();

        }
    }
}
