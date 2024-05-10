package com.example.miniproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi,asang;
    LabeledSwitch btnPUMP,auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        asang = findViewById(R.id.txtasang);

//        btnLED = findViewById(R.id.btnLED);
        btnPUMP = findViewById(R.id.btnPump);
        auto= findViewById(R.id.auto);


        btnPUMP.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
                // Implement your switching logic here
                if(isOn == true) {
                    sendDataMQTT("emcutene/feeds/nutnhan2", "1");
                }else {
                    sendDataMQTT("emcutene/feeds/nutnhan2", "0");
                }
            }
        });
//        btnLED.setOnToggledListener(new OnToggledListener() {
//            @Override
//            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
//                // Implement your switching logic here
//                if(isOn == true) {
//                    sendDataMQTT("emcutene/feeds/nutnhan1", "1");
//                }else {
//                    sendDataMQTT("emcutene/feeds/nutnhan1", "0");
//                }
//            }
//        });

        startMQTT();
//        fetchData();
    }
//    public void fetchData() {
//        // Gửi yêu cầu fetch dữ liệu tại đây
//        // Ví dụ:
//        sendDataMQTT("emcutene/feeds/nutnhan2", ""); // Gửi một tin nhắn trống để yêu cầu dữ liệu từ server
//    }

    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        }catch (MqttException e){
        }
    }
    public void startMQTT(){
        mqttHelper = new MQTTHelper(this);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
//                sendDataMQTT("emcutene/feeds/nutnhan2", "");
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("TEST", topic + "***" + message.toString());
                if(topic.contains("cambien3")) {
                    txtTemp.setText(message.toString() + "°C");
                }else if(topic.contains("cambien2")){
                    txtHumi.setText(message.toString() + "%");
                    int value = Integer.parseInt(message.toString());
                   if(auto.isOn()){
                       if (value <= 65) {
                           btnPUMP.setOn(true);
                           sendDataMQTT("emcutene/feeds/nutnhan2", "1");
                       } else if(value >=70){
                           // Tắt nút
                           btnPUMP.setOn(false);
                           sendDataMQTT("emcutene/feeds/nutnhan2", "0");
                       }
                   }
                }
                else if(topic.contains("cambien1"))
                    asang.setText(message.toString());
                else if(topic.contains("nutnhan2")){
                    if(message.toString().equals("1")) {
                        btnPUMP.setOn(true);
                    }else if(message.toString().equals("0")){
                        btnPUMP.setOn(false);
                    }
                }
//                else if(topic.contains("nutnhan1")){
//                    if(message.toString().equals("1")) {
//                        btnPUMP.setOn(true);
//                    }else if(message.toString().equals("0")){
//                        btnPUMP.setOn(false);
//                    }
//                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}