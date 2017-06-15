package org.limmen.mqtt.device;

import io.vertx.core.AbstractVerticle;
import java.util.Random;
import java.util.UUID;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends AbstractVerticle implements MqttCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private MqttClient client;

    private final MqttConnectOptions connectOptions = new MqttConnectOptions();

    private String id;
    
    private final Random random = new Random();

    @Override
    public void connectionLost(Throwable thrwbl) {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    }

    @Override
    public void start() {

        this.id = UUID.randomUUID().toString();

        try {
            createMqttClient();
        }
        catch (MqttException ex) {
            LOGGER.error("Failed to create MQTT client...", ex);
            return;
        }

        sendTemperature();
        sendHumidity();
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

        client = new MqttClient("tcp://" + host + ":" + port, id);
        client.setCallback(this);
        client.connect(connectOptions);
    }

    private void sendTemperature() {
        MqttTopic topic = client.getTopic("/demo/devices/" + id + "/temperature");
        vertx.setPeriodic(config().getInteger("device.temperature.interval.seconds", 5) * 1000, event -> {
            double val = 10 + (random.nextDouble() * 20);
            String valStr = val + "";

            try {
                LOGGER.info("Sending new temperature {}...", valStr);
                topic.publish(valStr.getBytes(), 0, false).waitForCompletion();
            }
            catch (MqttException ex) {
                LOGGER.error("Failed to send message", ex);
            }
        });
    }

    private void sendHumidity() {
        MqttTopic topic = client.getTopic("/demo/devices/" + id + "/humidity");
        vertx.setPeriodic(config().getInteger("device.himidity.interval.seconds", 5) * 1000, event -> {
            double val = 30 + (random.nextDouble() * 50);
            String valStr = val + "";

            try {
                LOGGER.info("Sending new humidity {}...", valStr);
                topic.publish(valStr.getBytes(), 0, false).waitForCompletion();
            }
            catch (MqttException ex) {
                LOGGER.error("Failed to send message", ex);
            }
        });
    }
}
