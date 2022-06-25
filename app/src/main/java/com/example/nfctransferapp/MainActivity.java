package com.example.nfctransferapp;

import android.app.Activity;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity implements
        CreateNdefMessageCallback, OnNdefPushCompleteCallback{

    TextView textInfo;
    EditText textOut;
    TextView deviceID;
    TelephonyManager tm;
    NfcAdapter nfcAdapter;
    String AndroidID;
    String credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInfo = (TextView)findViewById(R.id.info);
        textOut = (EditText)findViewById(R.id.textout);
        deviceID = (TextView)findViewById(R.id.device_id);

        AndroidID = Settings.Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        deviceID.setText(AndroidID.toString());

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
            Toast.makeText(MainActivity.this,
                    "nfcAdapter==null, no NFC adapter exists",
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(MainActivity.this,
                    "Set Callback(s)",
                    Toast.LENGTH_LONG).show();
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }

    public void btnMD5 (View v) {
        EditText firstName = (EditText)findViewById(R.id.first_name);
        EditText lastName = (EditText)findViewById(R.id.last_name);
        EditText middleName = (EditText)findViewById(R.id.middle_name);
        EditText birthdate = (EditText)findViewById(R.id.birthdate);
        TextView md5Output = (TextView)findViewById(R.id.md5_output);

        String firstNameS = new String(String.valueOf(firstName.getText()));
        String lastNameS = new String(String.valueOf(lastName.getText()));
        String middleNameS = new String(String.valueOf(middleName.getText()));
        String birthdateS = new String(String.valueOf(birthdate.getText()));

        credentials = firstNameS + lastNameS + middleNameS + birthdateS + AndroidID.toString();

        byte[] md5Input = credentials.getBytes();
        BigInteger md5Data = null;

        try {
            md5Data = new BigInteger(1, md5.encryptMD5(md5Input));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String md5Str = md5Data.toString(16);
        if (md5Str.length() < 32) {
            md5Str = 0 + md5Str;
        }
        md5Output.setText(md5Str);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage)parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];
            String inMsg = new String(NdefRecord_0.getPayload());
            textInfo.setText(inMsg);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {

        final String eventString = "onNdefPushComplete\n" + event.toString();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        eventString,
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        String stringOut = textOut.getText().toString();
        byte[] bytesOut = stringOut.getBytes();

        NdefRecord ndefRecordOut = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[] {},
                bytesOut);

        NdefMessage ndefMessageout = new NdefMessage(ndefRecordOut);
        return ndefMessageout;
    }

}