package com.example.scanprint;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import java.io.UnsupportedEncodingException;



public class MainActivity extends AppCompatActivity {

    private EditText mScanned;
    private ImageButton mScanButton;
    private ImageButton mPrintButton;
    Context context;
    final static String softScanTrigger = "com.symbol.datawedge.api.ACTION";
    final static String extraData = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScanned = findViewById(R.id.editTextScanned);
        mScanButton = findViewById(R.id.imageButtonScan);
        mPrintButton = findViewById(R.id.imageButtonPrint);
        context = getApplicationContext();

        mScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(softScanTrigger);
                i.putExtra(extraData, "START_SCANNING");
                sendBroadcast(i);
            }
        });

        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String zplData = new String();
                zplData = "^XA^XFE:A.ZPL^FS^FN1^FD" + mScanned.getText() + "^FS^XZ";
                byte[] passthroughBytes = null;
                try{
                    passthroughBytes = zplData.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e){
                    //Handle exception
                }

                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.zebra.printconnect",
                        "com.zebra.printconnect.print.PassthroughService"));
                intent.putExtra("com.zebra.printconnect.PrintService.PASSTHROUGH_DATA", passthroughBytes);
                intent.putExtra("com.zebra.printconnect.PrintSevice.RESULT_RECEIVER" , buildIPCSafeReceiver(new ResultReceiver(null){
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData){
                        if (resultCode == 0){//Result Code 0 indicates success
                            // Handle successfull print
                        } else {
                            String ErrorMessage = resultData.getString("com.zebra.printconnect.PrintService.ERROR_MESSAGE");
                        }
                    }
                }));
                startService(intent);
                mScanned.setText("");
            }
        });


    }
    private ResultReceiver buildIPCSafeReceiver(ResultReceiver actualReceiver){
        Parcel parcel = Parcel.obtain();
        actualReceiver.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

}
