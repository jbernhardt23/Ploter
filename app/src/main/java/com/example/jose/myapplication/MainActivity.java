package com.example.jose.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private int eraseColor = 0xFFFFFF;
    private DrawingView drawView, drawCanvas, drawPaint;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, undoBtn;
    private float largeBrush;
    private Socket socket;
    private static final int SERVERPORT = 5000;
    private static final String SERVER_IP = "10.28.223.92";

    //Context context = getApplicationContext();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //  setSupportActionBar(toolbar);

        largeBrush = getResources().getInteger(R.integer.large_size);

        drawView = (DrawingView) findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);

        //Instanciar el boton de borrar y responer al evento de clickErase
        eraseBtn = (ImageButton) findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);
        //Instanciar el boton de nueva pagina y responer al evento de clickNew
        newBtn = (ImageButton) findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);
        //Instanciar el boton de guardar y responder al evento
        saveBtn = (ImageButton) findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        //Instanciar el boton de guardar y responder al evento
        undoBtn = (ImageButton) findViewById(R.id.draw_btn);
        undoBtn.setOnClickListener(this);


        //Llamar el thread de telnet
        new Thread(new ClientThread()).start();
        Toast.makeText(getApplicationContext(),
                "Thread initiated", Toast.LENGTH_LONG).show();


    }

    public void paintClicked(View view) {
        //use chosen color

        //Ponter a pintar otra vez

        if (view != currPaint) {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            drawView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;

        }


    }

    public void onSaveClicked() {

        try {
            EditText et = (EditText) findViewById(R.id.editText);
            String str = et.getText().toString();
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(str);

            Toast.makeText(getApplicationContext(),
                    "Connection requested", Toast.LENGTH_LONG).show();
        } catch (UnknownHostException e) {
            Toast.makeText(getApplicationContext(),
                    "Unknown Host", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),
                    "Unable to connect to host", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Unable to connect to host plot", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        //respond to clicks

        if (view.getId() == R.id.draw_btn) {
            //draw button click

            if (!drawView.pointsStartXList.isEmpty()) {

                drawView.undo();

            } else {


                AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
                saveDialog.setTitle("Error");
                saveDialog.setMessage("Nothing to undo anymore.");
                saveDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                saveDialog.show();

            }


        } else if (view.getId() == R.id.erase_btn) {
            //switch to erase - choose size

            if (!drawView.redoStartXList.isEmpty()) {

                drawView.redo();
            } else {


                AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
                saveDialog.setTitle("Error");
                saveDialog.setMessage("Nothing to redo anymore.");
                saveDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                saveDialog.show();
            }


        } else if (view.getId() == R.id.new_btn) {
            //new button
            //Dialogo para crear nuevo Plott
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New Plot");
            newDialog.setMessage("Start new Plot (you will lose the current Plotting)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            newDialog.show();
        } else if (view.getId() == R.id.save_btn) {
           /* //sent plot
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Send Plot");
            saveDialog.setMessage("Send Plot to the Plot Machine?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            saveDialog.show();*/
            onSaveClicked();

        }

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

    class ClientThread implements Runnable {

        @Override

        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);
                Toast.makeText(getApplicationContext(),
                        "Connection established", Toast.LENGTH_SHORT).show();

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }


    }


}



