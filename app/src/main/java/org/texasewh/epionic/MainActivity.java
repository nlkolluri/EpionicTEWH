package org.texasewh.epionic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {



    private final String DEVICE_ADDRESS="98:D3:71:FD:4C:B6";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    boolean deviceConnected=false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;

    TextView status;

    private TextView mTextMessage;
    private Button btnStringA;
    public  TextView dataDisplay;
    private String inputString = "45,23#23,43#99,99#00,11,22,33,44,55,66,77,88,99,1010";
    private String probeString = "#";
    private String subSplitter = ",";

    String holder = "";
    int waitCounter=0;

   // private StringAnalyzer firstAnalyzer = new StringAnalyzer(inputString, probeString, subSplitter);
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_notes:
                    mTextMessage.setText(R.string.title_notes);
                    return true;
                case R.id.navigation_settings:
                    mTextMessage.setText(R.string.title_settings);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        dataDisplay = findViewById(R.id.test);
        status = (TextView) findViewById(R.id.message);

        //onClickStart();

        /*bluetooth methods here*/
        //setUiEnabled(false);

        /*bluetooth methods above*/
        /*firstAnalyzer.startParse();*/

        btnStringA = findViewById(R.id.btn_stringTest);
        btnStringA.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onClickStart();

           /*    firstAnalyzer.displayTemps(dataDisplay); */


           }
       });
    }

  /*  public void setUiEnabled(boolean bool)
    {
        start.setEnabled(!bool);
        status.setEnabled(bool);
    }*/



    public boolean BTinit()
    {
       // status.append("entered BTinit");
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
      //  status.append("test");
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
           // status.append("bluetooth not supported");
        }
        if(!bluetoothAdapter.isEnabled())
        {
            //status.append("bluetooth adapter not enabled");
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
            //status.append("pair device");

        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                   // status.append("checked address");
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect()
    {
        status.append(" entered bTconnect");
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }
    public void onClickStart() {
        status.append("clicked on start");
        if(BTinit())
        {
            if(BTconnect())
            {
               // setUiEnabled(true);
                deviceConnected=true;
                beginListenForData();
                status.append("\nConnection Opened!\n");
            }

        }
    }
    void beginListenForData()
    {
        status.append("start beginListen method");
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];

        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");

                            handler.post(new Runnable() {
                                public void run()
                                {
                                   /* if (waitCounter<20) {
                                        holder += string;
                                        waitCounter++;
                                    } else {*/
                                        //dataDisplay.setText("Run reached");
                                      //  status.append("\n" + holder);
                                     //   StringAnalyzer firstAnalyzer = new StringAnalyzer(holder, "#", ",");
                                    status.append(string);
                                    status.append("\n");
                                    StringAnalyzer firstAnalyzer = new StringAnalyzer(string, "#", ",");
                                    if (firstAnalyzer.startParse(dataDisplay)) {
                                        firstAnalyzer.displayTemps(dataDisplay);
                                    }
                                    //dataDisplay.setText(dataDisplay.getText()+"\n"+string );
                                     /*   if (firstAnalyzer.startParse(dataDisplay)) {
                                            firstAnalyzer.displayTemps(dataDisplay);
                                            // holder="";
                                            // waitCounter=0;
                                        }*/

                                  //  } -- bracket for else
                                    /* Below statement could cause an error with cutting off last digit */
                                    //Potential loop - not now while (firstAnalyzer.getTempArray().length-1>firstAnalyzer.getTempCounter()) {}
                                       // firstAnalyzer.displayTemps(dataDisplay);




                                }
                            });

                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }


}
