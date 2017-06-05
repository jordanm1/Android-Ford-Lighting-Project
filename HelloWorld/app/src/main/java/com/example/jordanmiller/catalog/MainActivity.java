package com.example.jordanmiller.catalog;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private OutputStream packetStreamOut;
    private SendLightInfoTask networkTask;

    private static final int WIDTH = 720;
    private static final int HEIGHT = 1280;

    private int xTouch = 0;
    private int yTouch = 0;
    private byte[] packet = new byte[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View mainView = findViewById(R.id.mainView);
        mainView.setOnTouchListener(handleTouch);

        networkTask = new SendLightInfoTask();

        ImageButton tcptest = (ImageButton) findViewById((R.id.tcptest));
        tcptest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    new SendLightInfoTask().execute();
                    Toast.makeText(MainActivity.this, "Packet sent to 100.121.96.5", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Not connected to network", Toast.LENGTH_SHORT).show();
                }
                Log.d("MainActivity", "tcptest Clicked");
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        SeekBar lightIntensity = (SeekBar) findViewById(R.id.seekBar);
        lightIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                //Toast.makeText(getApplicationContext(), "Light Instensity: " + progress + "%", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //textView.setText("Covered: " + progress + "/" + seekBar.getMax());
                Toast.makeText(getApplicationContext(), "Light Instensity: " + progress + "%", Toast.LENGTH_SHORT).show();
            }
        });

//        lightIntensity.setOnSeekBarChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScroll(View view) {
//                lightIntensity.
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()){
            return true;
        }
        else {
            return false;
        }
    }

    private class SendLightInfoTask extends AsyncTask<Void, Void, Void> {
        Socket socket = null;
        OutputStream packetStreamOut = null;
        InputStream packetStreamIn = null;

        protected Void doInBackground(Void... arg0) {
            try {
                Log.d("MainActivity", "socket attempt");
                //socket = new Socket("171.64.50.233", 55056);
                socket = new Socket("166.250.214.115", 2000);
                Log.d("MainActivity", "backgroundSocketCreated with packet " + packet[0] + packet[1] + packet[2] + packet[3]);
                packetStreamOut = socket.getOutputStream();
                //packetStreamIn = socket.getInputStream();
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (packetStreamOut != null) {
                        packetStreamOut.write(packet);
                        packetStreamOut.close();
                        int i = 0;
                        Log.d("MainActivity", "writtenToStream");
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }


            return null;
        }


    }

    private View.OnTouchListener handleTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            xTouch = (int) event.getX() - (WIDTH/2);//)  *1000)/(WIDTH/2);
            yTouch = (int) -event.getY() + (HEIGHT/2);//) *1000)/(HEIGHT/2);
            packet[0] = (byte) 0xa0;
            packet[1] = (byte) (((int) xTouch));
            packet[2] = (byte) ((int) xTouch >> 8);
            packet[3] = (byte) (((int) yTouch));
            packet[4] = (byte) ((int) yTouch >> 8);


            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("TAG", "touched down");
                    Toast.makeText(MainActivity.this, "Moving Light to " + xTouch + ", " + yTouch + "\n" +
                            "1:0x" + Integer.toHexString(packet[0]) + " 2:0x" + Integer.toHexString(packet[1]) + " 3:0x" + Integer.toHexString(packet[2]), Toast.LENGTH_LONG).show();
                    int x2 = xTouch - yTouch;
                    Log.i("TAG", "party" + x2);
                    new SendLightInfoTask().execute();
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.i("TAG", "moving: (" + xTouch + ", " + yTouch + ")");
                    //Toast.makeText(MainActivity.this, "Touch at " + xTouch + ", " + yTouch, Toast.LENGTH_SHORT).show();
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up");
                    break;
            }

            return true;
        }
    };
}
