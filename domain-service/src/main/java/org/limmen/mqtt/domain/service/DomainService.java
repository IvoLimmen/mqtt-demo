package org.limmen.mqtt.domain.service;

import io.vertx.core.AbstractVerticle;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainService extends AbstractVerticle implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainService.class);

    private MqttClient client;

    private final MqttConnectOptions connectOptions = new MqttConnectOptions();

    @Override
    public void start() throws Exception {
        createMqttClient();
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        LOGGER.error("Connection lost...", thrwbl);        
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        LOGGER.info("Delivery complete...");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String data = new String(message.getPayload());
        LOGGER.info("Message arrived: {} on {}", data, topic);
    }

    private void createMqttClient() throws MqttException {
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setMaxInflight(config().getInteger("mqtt.maxinflight", 1000));
        
        LOGGER.info("Starting domain service...");

        String host = config().getString("mqtt.host", "localhost");
        Integer port = config().getInteger("mqtt.port", 1883);
        
        client = new MqttClient("tcp://" + host + ":" + port, "domain-service");
        client.setCallback(this);
        client.connect(connectOptions);

        client.subscribe("/demo/devices/+/temperature", 0, this::temperatureMessageHandler);
        client.subscribe("/demo/devices/+/humidity", 0, this::humidityMessageHandler);

        LOGGER.info("Subscribed...");
    }

    private void temperatureMessageHandler(String topic, MqttMessage message) throws MqttException {
        String data = new String(message.getPayload());
        LOGGER.info("Received {} on {}", data, topic);

        try {
            double temp = Double.parseDouble(data);

            if (temp >= 25) {
                LOGGER.info("Received {} on {}", data, topic);
                MqttMessage msg = new MqttMessage();
                String str = "Temperature " + data + " exceeds threshold of 25 on device " + topic;
                msg.setPayload(str.getBytes());
                msg.setQos(2);
                client.getTopic("/demo/alert").publish(msg);
            }
        }
        catch (NumberFormatException nfe) {
            LOGGER.warn("Formatting exception...", nfe);
        }
    }

    private void humidityMessageHandler(String topic, MqttMessage message) throws MqttException {
        String data = new String(message.getPayload());
        LOGGER.info("Received {} on {}", data, topic);

        try {
            double hum = Double.parseDouble(data);

            if (hum >= 65) {
                LOGGER.info("Received {} on {}", data, topic);
                MqttMessage msg = new MqttMessage();
                String str = "Humidity " + data + " exceeds threshold of 65 on device " + topic;
                msg.setPayload(str.getBytes());
                msg.setQos(2);
                client.getTopic("/demo/alert").publish(msg);
            }
        }
        catch (NumberFormatException nfe) {
            LOGGER.warn("Formatting exception...", nfe);
        }
    }
}
