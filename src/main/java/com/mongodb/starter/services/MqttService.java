package com.mongodb.starter.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.mongodb.starter.models.SensorData;
import com.mongodb.starter.repositories.SensorDataRepository;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;

@Service
public class MqttService implements CommandLineRunner {

    @Autowired
    private SensorDataRepository sensorDataRepository;
    private final String host = "5268996a427b4742bc30f7ec3ea264ee.s1.eu.hivemq.cloud";

    private final String username = "nguyenhaiyen";
    private final String password = "B21dccn129@";


    @Override
    public void run(String... args) throws Exception {
        startMqtt();
    }

    public void startMqtt() {
        try {
            final Mqtt5BlockingClient client = MqttClient.builder()
                    .useMqttVersion5()
                    .serverHost(host)
                    .serverPort(8883)
                    .sslWithDefaultConfig()
                    .buildBlocking();

            client.connectWith()
                    .simpleAuth()
                    .username(username)
                    .password(StandardCharsets.UTF_8.encode(password))
                    .applySimpleAuth()
                    .send();

            System.out.println("Connected successfully");

            client.subscribeWith()
                    .topicFilter("B21DCCN129/esp8266/sensor_data")
                    .send();

            client.toAsync().publishes(ALL, publish -> handleIncomingMessage(publish));
        } catch (Exception e) {
            System.err.println("Error starting MQTT client: " + e.getMessage());
        }
    }

    private void handleIncomingMessage(Mqtt5Publish publish) {
        try {
            String message = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();

            float temperature = json.get("temperature").getAsFloat();
            int roundedTemperature = Math.round(temperature);

            float humidity = json.get("humidity").getAsFloat();
            int roundedHumidity = Math.round(humidity);

            float light = json.get("light").getAsFloat();
            int roundedLight = Math.round(light);
            long timestamp = json.get("timestamp").getAsLong();

            Random random = new Random();
            int wind = random.nextInt(101); // Tạo số ngẫu nhiên từ 0 đến 100

            saveSensorData(roundedTemperature, roundedHumidity, roundedLight, wind);
        } catch (Exception e) {
            System.err.println("Error processing incoming message: " + e.getMessage());
        }
    }

    private void saveSensorData(int temperature, int humidity, int light, int wind) {

        LocalDateTime now = LocalDateTime.now();
        // Định dạng thời gian
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

        // Chuyển LocalDateTime sang String
        String formattedTime = now.format(formatter);
        SensorData sensorData = new SensorData()
                .temperature(temperature)
                .humidity(humidity)
                .light(light)
                .wind(wind)
                .timeStr(formattedTime)
                .time(now);

        sensorDataRepository.save(sensorData);
//        System.out.println("Stored data in MongoDB: " + sensorData);
    }
    public void sendMessage(String topic, String message) {
        System.out.println("Sending message to topic: " + topic);
        System.out.println("Message content: " + message);

        Mqtt5AsyncClient client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildAsync();

        client.connectWith()
                .simpleAuth()
                .username(username)
                .password(StandardCharsets.UTF_8.encode(password))
                .applySimpleAuth()
                .send()
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        System.err.println("Connection failed: " + throwable.getMessage());
                    } else {
                        System.out.println("Connected successfully");
                        client.publishWith()
                                .topic(topic)
                                .payload(StandardCharsets.UTF_8.encode(message))
                                .send()
                                .whenComplete((pubResult, pubThrowable) -> {
                                    if (pubThrowable != null) {
                                        System.err.println("Publish failed: " + pubThrowable.getMessage());
                                    } else {
                                        System.out.println("Message published successfully");
                                    }
                                });
                    }
                });
    }



}

