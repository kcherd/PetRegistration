package com.example.petreg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
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

public class MainActivity extends AppCompatActivity implements Listener {

    private static final String TAG_MAIN = "mainTag";
    private FragmentManager fragmentManager;
    private NfcAdapter mAdapter;

    private InfoFragment infoFragment;
    private RegisterFragment registerFragment;

    private boolean isWrite = false;
    private boolean isDialogDisplayed = false;

    public boolean getIsWrite() {
        return isWrite;
    }

    public void setIsWrite(boolean write) {
        isWrite = write;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, new StartFragment())
                .addToBackStack(null)
                .commit();

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        Intent intent = getIntent();
        openInfoFragment(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mAdapter!= null)
            mAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAdapter!= null)
            mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            if (isDialogDisplayed) {
                if (isWrite) {

                    String messageToWrite = "testMess"; //mEtMessage.getText().toString();
                    registerFragment = (RegisterFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                    registerFragment.onNfcDetected(ndef, messageToWrite);

                } else {
                    infoFragment = (InfoFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                    Log.d(TAG_MAIN, "infoFragment object: " + infoFragment);
                    infoFragment.onNfcDetected(ndef);
                }
            }

        }
    }

    public void openInfoFragment(Intent intent){

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

    public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8){
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }

//    @Override
//    public void registerEvent(String id) {
//
//        Log.d(TAG_MAIN, "registerEvent: " + id);
//        RecordDialogFragment dialogFragment = new RecordDialogFragment();
//
//        NdefMessage mNdefPushMessage = new NdefMessage(new NdefRecord[]  {createTextRecord(id, Locale.ENGLISH, true)});
//        mAdapter.setNdefPushMessage(mNdefPushMessage, this);
//        dialogFragment.show(getSupportFragmentManager(), "Record");
//    }

    @Override
    public void onDialogDisplayed() {
        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {
        isDialogDisplayed = false;
        isWrite = false;
    }
}