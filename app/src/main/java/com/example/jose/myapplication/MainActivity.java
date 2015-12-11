package com.example.jose.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final int SERVERPORT = 23;
    private static String responseError = null;
    EditText textAddress;
    EditText portAddress;
    private DrawingView drawView, drawCanvas, drawPaint, startY, endX, endY, start;
    private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn, undoBtn;
    private Button telnetSend;
    private float largeBrush;
    private Socket socket;

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

        //Connection info buttons
        textAddress = (EditText) findViewById(R.id.ipServer);
        portAddress = (EditText) findViewById(R.id.portServer);
        telnetSend = (Button) findViewById(R.id.telnetSend);


        //Asingar conexion al boton de enviar Connection
        OnClickListener telnetSendOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                MyClientTask myClientTask = new MyClientTask(textAddress.getText().toString()
                        , Integer.parseInt(portAddress.getText().toString()));
                myClientTask.execute();
            }
        };

        telnetSend.setOnClickListener(telnetSendOnClickListener);



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



    @Override
    public void onClick(View view) {
        //Boton de Undo

        if (view.getId() == R.id.draw_btn) {


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
            //Boton de redo

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
            saveDialog.show();


        }


    }

    //Thread para correr el telnet Client

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

    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response = "";

        MyClientTask(String addr, int port) {
            dstAddress = addr;
            dstPort = port;

            Toast.makeText(getApplicationContext(), "Requesting Connection...",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Socket socket = null;

            try {
                socket = new Socket(dstAddress, dstPort);

                DataOutputStream sendInfo = new DataOutputStream(socket.getOutputStream());

                /*for (int i = 0; i < DrawingView.pointsStartXList.size(); i++) {


                    sendInfo.writeUTF(DrawingView.pointsStartXList.get
                            (i).toString() + " " + DrawingView.pointsStartYList.get
                            (i).toString() + " " + DrawingView.pointsEndXList.get
                            (i).toString() + " " + DrawingView.pointsEndYList.get
                            (i).toString());

                    sendInfo.flush();

                }*/

                sendInfo.writeFloat(DrawingView.pointsStartXList.get
                        (DrawingView.pointsStartXList.size() - 1).intValue());

                // Send the exit message
                //sendInfo.writeByte(-1);
                sendInfo.flush();

                sendInfo.close();

                //***************************************************************//

                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();


                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");


                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } catch (Exception e) {
                e.printStackTrace();
                response = "Exception: " + e.toString();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            responseError = response.toString();
            Toast.makeText(getApplicationContext(), responseError,
                    Toast.LENGTH_LONG).show();
        }


    }


}



