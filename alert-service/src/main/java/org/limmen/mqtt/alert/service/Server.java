package org.limmen.mqtt.alert.service;

import io.vertx.core.AbstractVerticle;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends AbstractVerticle implements MqttCallback {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    
    private MqttClient client;

    private final MqttConnectOptions connectOptions = new MqttConnectOptions();

    @Override
    public void connectionLost(Throwable thrwbl) {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
    }

    @Override
    public void start() {

        try {
            createMqttClient();
        }
        catch (MqttException ex) {
            ex.printStackTrace();
            return;
        }
    }

    @Override
    public void stop() throws Exception {
        client.close();
    }

    private void createMqttClient() throws MqttException {
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setCleanSession(false);
        connectOptions.setMaxInflight(config().getInteger("mqtt.maxinflight", 1000));

        String host = config().getString("mqtt.host", "localhost");
        Integer port = config().getInteger("mqtt.port", 1883);
        
        client = new MqttClient("tcp://" + host + ":" + port, "alert-service");
        client.setCallback(this);
        client.connect(connectOptions);
        
        client.subscribe("/demo/alert", this::messageHandler);
    }
    
    private void messageHandler(String topic, MqttMessage message) {
        LOGGER.warn(new String(message.getPayload()));
    }
}
