package iot.bolt.IntensityController;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import iot.bolt.intensitycontroller.R;

public class MainActivity extends AppCompatActivity {
    private final String APIKey = "2a4660f1-5a0f-47e3-afcd-2e1644d1c337";
    private String ID = "BOLT5777301";
    private String status = "Unknown";
    private TextView mTextView;
    private TextView mIntensity;
    private SeekBar mSeekBar;
    private int mValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.boltStatus);
        mIntensity = findViewById(R.id.intensity);
        mSeekBar = findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mValue = seekBar.getProgress();
                mIntensity.setText(String.valueOf(mValue));
                Toast.makeText(getApplicationContext(), "getProgress" + mValue, Toast.LENGTH_SHORT).show();
                writeAnalogPin(0, mValue);

            }
        });


        isOnline();
    }

    public void isOnline() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://cloud.boltiot.com/remote/" + APIKey + "/isOnline?&deviceName=" + ID;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("Response", response.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (jsonObject.get("success").equals(1)) {
                                status = jsonObject.get("value").toString();
                                if (status.equals("online")) {
                                    Toast.makeText(getApplicationContext(), "online", Toast.LENGTH_SHORT).show();
                                    mTextView.setText("Online");
                                    mTextView.setTextColor(Color.GREEN);
                                } else if (status.equals("offline")) {
                                    Toast.makeText(getApplicationContext(), "offline", Toast.LENGTH_SHORT).show();
                                    mTextView.setText("offline");
                                    mTextView.setTextColor(Color.RED);
                                }
                            } else {
                                if (jsonObject.get("value").equals("Invalid API key")) {
                                    Toast.makeText(getApplicationContext(), "Invalid API key", Toast.LENGTH_SHORT).show();
                                    mTextView.setText("Invalid API key");
                                }
                                if ((jsonObject.get("value").equals("Device does not exist"))) {
                                    Toast.makeText(getApplicationContext(), "Device does not exist", Toast.LENGTH_SHORT).show();
                                    //boltStatus.getBoltStatus(DEVICE_NOT_FOUND);
                                    mTextView.setText("Device does not exist");
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });

        queue.add(stringRequest);

    }

    public void readDigitalPin(final int pin) {
        String Url = "https://cloud.boltiot.com/remote/" + APIKey + "/digitalRead?pin=" + pin + "&deviceName=" + ID;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        if (response != null) {
                            Log.d("VolleyResponse", "Response is: " + response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Log.d("VolleyJson", "" + jsonObject.get("value"));
                                status = jsonObject.get("value").toString();
                                if (status.equals("1")) {
                                    Toast.makeText(getApplicationContext(), "digitalRead : High", Toast.LENGTH_SHORT).show();
                                } else if (status.equals("0")) {
                                    Toast.makeText(getApplicationContext(), "digitalRead : Low", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VolleyResponse", "That didn't work!");
                //  boltStatus.getBoltStatus(CONNECTION_TIMEOUT);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }


    public void writeDigitalPin(final int pin, String state) {
        String Url = "https://cloud.boltiot.com/remote/" + APIKey + "/digitalWrite?pin=" + pin + "&state=" + state + "&deviceName=" + ID;
        Log.d("WriteCalled", "" + Url);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("VolleyResponse", "Response is: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d("VolleyJson", "" + jsonObject.get("value"));
                            status = jsonObject.get("value").toString();
                            if (jsonObject.get("success").equals(1)) {
                                if (status.equals("1")) {
                                    Toast.makeText(getApplicationContext(), "digitalWrite : ON ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "digitalWrite : OFF", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VolleyResponse", "That didn't work!");
                Toast.makeText(getApplicationContext(), "FAILED_TO_WRITE PWM", Toast.LENGTH_SHORT).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    /**
     * writeAnalogPin method is used to set value on particular pin.
     *
     * @param pin   represent the pin number.
     * @param state voltage value.
     */
    public void writeAnalogPin(final int pin, int state) {
        String Url = "https://cloud.boltiot.com/remote/" + APIKey + "/analogWrite?pin=" + pin + "&value=" + state + "&deviceName=" + ID;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("VolleyResponse", "Response is: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.d("VolleyJson", "" + jsonObject.get("value"));
                            status = jsonObject.get("value").toString();
                            if (jsonObject.get("success").equals(1)) {
                                if (status.equals("1")) {
                                    Toast.makeText(getApplicationContext(), "digitalWrite : ON PWM", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "digitalWrite : OFF PWM", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VolleyResponse", "That didn't work!");
                Toast.makeText(getApplicationContext(), "FAILED_TO_WRITE PWM", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }
}

