package com.example.petreg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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