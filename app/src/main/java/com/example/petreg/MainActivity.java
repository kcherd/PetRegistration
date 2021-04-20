package com.example.petreg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_MAIN = "mainTag";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, new StartFragment())
                .addToBackStack(null)
                .commit();

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Log.d(TAG_MAIN, "read nfc");

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }

            //записи с метки
            NdefRecord[] records =  msgs[0].getRecords();
            NdefRecord record = records[0];
            Log.d(TAG_MAIN, "record size = " + records.length);

            //извлечение текста из метки
            try{
                byte[] payload = record.getPayload();
                /*
                 * payload[0] содержит поле "Кодировки байтов состояния" в соответствии с
                 * NFC Forum "Text Record Type Definition" section 3.2.1.
                 *
                 * bit7 это поле кодировки текста
                 *
                 * if (Bit_7 == 0): текст закодирован в UTF-8 if (Bit_7 == 1):
                 * текст в кодировке UTF16
                 *
                 * Bit_6 зарезервирован для использования в будущем и должен быть установлен в ноль
                 *
                 * Биты с 5 по 0 - это длина кода языка IANA
                 */
                String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
                int languageCodeLength = payload[0] & 0077;
                String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                String text = new String(payload, languageCodeLength + 1,
                                payload.length - languageCodeLength - 1, textEncoding);

                Log.d(TAG_MAIN, "languageCode = " + languageCode + " text:" + text);

                //вызов фрагмента с информацией
                Bundle infoArg = new Bundle();
                infoArg.putString("id", text);
                InfoFragment infoFragment = new InfoFragment();
                infoFragment.setArguments(infoArg);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, infoFragment)
                        .addToBackStack(null)
                        .commit();
            }catch (UnsupportedEncodingException e) {
                // should never happen unless we get a malformed tag.
                throw new IllegalArgumentException(e);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        setIntent(intent);
//        String action = intent.getAction();
//        Log.d(TAG_MAIN, "onNewIntent: ");
//        tv.setText("HI");
//
//        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
//            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            NdefMessage[] msgs;
//            if (rawMsgs != null) {
//                msgs = new NdefMessage[rawMsgs.length];
//                for (int i = 0; i < rawMsgs.length; i++) {
//                    msgs[i] = (NdefMessage) rawMsgs[i];
//                }
//            }
//        }
    }

}