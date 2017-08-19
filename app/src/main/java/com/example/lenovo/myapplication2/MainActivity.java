package com.example.lenovo.myapplication2;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
//import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import static android.R.attr.data;


public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private TextView  x, y, z, magbefore, xsg, ysg, zsg, magafter,saveM;
    //private TextView etshowval;
    private SensorManager sensorManager;
    private Sensor sensor;


    private List<Double> arrayList;
    private Boolean Bool = false;
    private Boolean savebool = false;
    //private Boolean readbool = false;

    private String data = "";

    private GraphView graphView;
    private double values[];
    private double val[];
    private double filt[];
    private LineGraphSeries<DataPoint> series, filtered;


    private double[] xvaluesg = new double[5];
    private double[] yvaluesg = new double[5];
    private double[] zvaluesg = new double[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button start, stop, save;
        //final Button read;
        //initialize views
        x = (TextView) findViewById(R.id.xdata);
        y = (TextView) findViewById(R.id.ydata);
        z = (TextView) findViewById(R.id.zdata);

        //etshowval = (TextView)findViewById(R.id.showval);
        saveM = (TextView) findViewById(R.id.saveMessage);
        magbefore = (TextView) findViewById(R.id.magnitude_data_before);
        xsg = (TextView) findViewById(R.id.xdatasg);
        ysg = (TextView) findViewById(R.id.ydatasg);
        zsg = (TextView) findViewById(R.id.zdatasg);
        magafter = (TextView) findViewById(R.id.magnitude_data_after);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        save = (Button) findViewById(R.id.save);
        //read = (Button) findViewById(R.id.read);
        graphView = (GraphView) findViewById(R.id.graph);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(50);


        for (int i = 0; i < 5; i++) {
            xvaluesg[i] = 0;
            yvaluesg[i] = 0;
            zvaluesg[i] = 0;
        }


        //SAVE BUTTON
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(savebool){
                    File myDir = new File(Environment.getExternalStorageDirectory(), "accelerometer/");
                    String filename = "accelerometer"+System.currentTimeMillis()+".txt";
                    try {
                        boolean res = myDir.mkdirs();
                        File file = new File(myDir, filename);
                        res = res ^ file.createNewFile();
                        System.out.print(res);
                        PrintWriter out = new PrintWriter(file);
                        out.write(data);
                        out.flush();
                        out.close();
                        Toast.makeText(getApplicationContext(), String.format(Locale.getDefault(),getString(R.string.fsvdat), myDir, filename), Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), getString(R.string.fail)+e, Toast.LENGTH_SHORT).show();
                    }
                    saveM.setText(getString(R.string.not_saving));
                }
                else {
                    saveM.setText(getString(R.string.not_saving));
                    Toast.makeText(getApplicationContext(), R.string.started, Toast.LENGTH_SHORT).show();
                    data = "";
                }
                System.out.print(v);
                    savebool=false;
            }
        });

        //START BUTTON
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayList = new ArrayList<>();
                v.setClickable(false);
                Bool = true;
                graphView.removeAllSeries();
                savebool=true;

            }
        });

        //STOP BUTTON
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bool = false;
                start.setClickable(true);
                Double[] arr = arrayList.toArray(new Double[arrayList.size()]);
                values = new double[arr.length];
                for (int j = 0; j < arr.length; j++) {
                    values[j] = arr[j];
                }
                SGFilter sgFilter = new SGFilter(3, 3);
                filt = SGFilter.computeSGCoefficients(3, 3, 4);
                val = sgFilter.smooth(values, filt);
                series = new LineGraphSeries<>();
                series.setColor(Color.RED);
                filtered = new LineGraphSeries<>();
                filtered.setColor(Color.BLUE);
                for (int i = 0; i < values.length; i++) {
                    series.appendData(new DataPoint(i, values[i]), true, 100);
                }
                for (int i = 0; i < val.length; i++) {
                    filtered.appendData(new DataPoint(i, val[i]), true, 100);
                }
                graphView.addSeries(series);
                graphView.addSeries(filtered);
                graphView.getViewport().setScalable(true);
                graphView.getViewport().setScrollable(true);
                graphView.getViewport().setScalableY(true);
                graphView.getViewport().setScrollableY(true);
                savebool=false;

            }
        });




        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        double xval,yval,zval,xsgval,ysgval,zsgval,magaftervalue,magbeforevalue;

        xval = event.values[0];
        yval = event.values[1];
        zval = event.values[2];
        magbeforevalue = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1]
                + event.values[2] * event.values[2]);
        xval = Double.parseDouble(String.format(Locale.getDefault(),"%.4f",xval));
        yval = Double.parseDouble(String.format(Locale.getDefault(),"%.4f",yval));
        zval = Double.parseDouble(String.format(Locale.getDefault(),"%.4f",zval));
        magbeforevalue = Double.parseDouble(String.format(Locale.getDefault(),"%.4f",magbeforevalue));
        x.setText(getString(R.string.xaccel ,xval));
        y.setText(getString(R.string.yaccel ,yval));
        z.setText(getString(R.string.zaccel ,zval));
        magbefore.setText(getString(R.string.magaccel ,magbeforevalue));

        for(int i=0;i<4;i++){
            xvaluesg[i] = xvaluesg[i+1];
            yvaluesg[i] = yvaluesg[i+1];
            zvaluesg[i] = zvaluesg[i+1];
        }
        xvaluesg[4] = event.values[0];
        yvaluesg[4] = event.values[1];
        zvaluesg[4] = event.values[2];
        xsgval = (-3*xvaluesg[0]+12*xvaluesg[1]+17*xvaluesg[2]+12*xvaluesg[3]-3*xvaluesg[4])/35;
        ysgval = (-3*yvaluesg[0]+12*yvaluesg[1]+17*yvaluesg[2]+12*yvaluesg[3]-3*yvaluesg[4])/35;
        zsgval = (-3*zvaluesg[0]+12*zvaluesg[1]+17*zvaluesg[2]+12*zvaluesg[3]-3*zvaluesg[4])/35;
        magaftervalue = Math.sqrt(xsgval*xsgval+ysgval*ysgval+zsgval*zsgval);
        xsgval = Double.parseDouble(String.format(Locale.getDefault(),"%.4f",xsgval));
        ysgval = Double.parseDouble(String.format(Locale.getDefault(),"%.4f",ysgval));
        zsgval = Double.parseDouble(String.format(Locale.getDefault(),"%.4f",zsgval));
        magaftervalue = Double.parseDouble(String.format(Locale.getDefault(),"%.4f",magaftervalue));
        xsg.setText(getString(R.string.xsgaccel ,xsgval));
        ysg.setText(getString(R.string.ysgaccel ,ysgval));
        zsg.setText(getString(R.string.zsgaccel ,zsgval));
        magafter.setText(getString(R.string.magsgaccel ,magaftervalue));


        if (Bool) {
            arrayList.add(Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]));
        }
        data+=(String.format(Locale.getDefault(), "%.4f", xval)+" "+String.format(Locale.getDefault(), "%.4f", yval)+" "+
                String.format(Locale.getDefault(), "%.4f", zval)+" " +"\n"+ String.format(Locale.getDefault(), "%.4f", xsgval)+" "
                +String.format(Locale.getDefault(), "%.4f", ysgval)+" "+ String.format(Locale.getDefault(), "%.4f", zsgval)+" "+
                System.currentTimeMillis()+"\n"+"\n");
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
