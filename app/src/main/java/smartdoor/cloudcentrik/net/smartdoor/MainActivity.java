package smartdoor.cloudcentrik.net.smartdoor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import io.particle.android.sdk.cloud.ApiFactory;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Toaster;

public class MainActivity extends AppCompatActivity {

    private TextView textDeviceName, textDeviceStatus, textTemperature,textLight,textHumidity,textDoorLock;
    private Button buttonUpdateData,buttonClearData;
    private ToggleButton buttonPlayMelody,buttonTurnLight;
    private static double temperatureReading = 0.0;
    private static double humidityReading = 0.0;
    private static int lightReading = 0;
    private static String doorLock = "";
    private static long subscriptionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //text view
        textDeviceName = (TextView) findViewById(R.id.device_name);
        textDeviceStatus = (TextView) findViewById(R.id.device_status);
        textTemperature = (TextView) findViewById(R.id.device_temperature);
        textLight = (TextView) findViewById(R.id.device_lightMeter);
        textHumidity = (TextView) findViewById(R.id.device_humidity);
        textDoorLock = (TextView) findViewById(R.id.device_door);

        //button play melody
        buttonPlayMelody=(ToggleButton)findViewById(R.id.button_play_melody);
        buttonPlayMelody.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    UiUtils.showInfo("Playing music on",getApplicationContext());
                } else {
                    // The toggle is disabled
                    UiUtils.showInfo("Playing music off",getApplicationContext());
                }
            }
        });

        //button turn light
        buttonTurnLight=(ToggleButton)findViewById(R.id.button_turn_light);
        buttonTurnLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Toast.makeText(getApplicationContext(),"Turning light on", Toast.LENGTH_LONG).show();
                } else {
                    // The toggle is disabled
                    Toast.makeText(getApplicationContext(),"Turning light off", Toast.LENGTH_LONG).show();
                }
            }
        });

        //button clear data
        buttonClearData=(Button)findViewById(R.id.button_clear_data);
        buttonClearData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                clearData();

            }
        });


        //button load data
        buttonUpdateData=(Button)findViewById(R.id.button_update_data);
        buttonUpdateData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                if(isConnectedToInternet(getApplicationContext())){
                    ParticleCloudSDK.init(getApplicationContext());
                    new ParticleLoginTask().execute();
                    new ParticeDeviceListTask(MainActivity.this).execute();
                }else {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Internet connection Error");
                    alertDialog.setMessage("Can not connect to internet. Check your network setting");
                    // Alert dialog button
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Alert dialog action goes here
                                    // onClick button code here
                                    dialog.dismiss();// use dismiss to cancel alert dialog
                                }
                            });
                    alertDialog.show();
                }

            }
        });

    }

    private class ParticleLoginTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            //  LOG IN TO PARTICLE
            try {
                // Log in to Particle Cloud using username and password
                ParticleCloudSDK.getCloud().logIn(ParticleCloudUtils.USER_EMAIL, ParticleCloudUtils.USER_PASSWORD);
                return "Logged in to particle cloud, reading device data";
            } catch (ParticleCloudException e) {
                Log.e("PHOTON", "Error logging in: " + e.toString());
                return "Error logging in!";
            }
        }

        protected void onPostExecute(String msg) {
            Toaster.s(MainActivity.this, msg);
        }
    }

    private class ParticeDeviceListTask extends AsyncTask<Void, Void, ParticleDevice> {
        private ProgressDialog dialog;

        public ParticeDeviceListTask(MainActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Connecting to Photon device, please wait...");
            dialog.show();
        }
        protected ParticleDevice doInBackground(Void... params) {

            ParticleDevice myDevice = null;
            try {
                List<ParticleDevice> devices = ParticleCloudSDK.getCloud().getDevices();
                for (ParticleDevice device : devices) {
                    if (device.getName().equals("cloudphoton1")) {
                        //device event
                        myDevice = device;
                        temperatureReading = device.getDoubleVariable("temperature");
                        humidityReading = device.getDoubleVariable("humidity");
                        lightReading=device.getIntVariable("lightLevel");
                        doorLock=device.getStringVariable("doorLock");
                        SystemClock.sleep(1000);
                        break;
                    }
                }
            } catch (ParticleDevice.VariableDoesNotExistException e) {
                Log.d("ERROR", e.getMessage());
                //Toaster.s(MainActivity.this, e.getMessage());

            } catch (IOException e) {
                Log.d("ERROR", e.getMessage());

            } catch (ParticleCloudException e) {
                Log.e("PHOTON", "Error logging in: " + e.toString());
                return null;
            }
            return myDevice;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(ParticleDevice device) {

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            textDeviceName.setText(device.getName());
            textDeviceStatus.setText(device.getStatus());
            textTemperature.setText(temperatureReading + " C");
            textLight.setText(lightReading + " Flux");
            textHumidity.setText(humidityReading+" %");
            textDoorLock.setText(doorLock);

        }
    }

    private boolean isConnectedToInternet(Context context){
        boolean isConnected=false;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }catch(Exception e) {
            Log.v("connectivity", e.toString());
        }

        return isConnected;


    }

    private void clearData(){
        textDeviceName.setText("Device Name");
        textDeviceStatus.setText("Device Status");
        textTemperature.setText("0 C");
        textLight.setText("0 Flux");
        textHumidity.setText(" 0%");
        textDoorLock.setText("Unknown");
    }

}
