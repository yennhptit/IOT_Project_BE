#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>
#include <ArduinoJson.h>
#include <WiFiClientSecure.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

// Định nghĩa chân cho DHT11, LED và quang trở
#define DHTPIN D1       // Chân DHT11
#define DHTTYPE DHT11   // Loại cảm biến DHT11
#define LDR_PIN A0      // Chân cảm biến ánh sáng

#define LED1 D2        // Chân GPIO4 (D2) - Fan
#define LED2 D5        // Chân GPIO14 (D5) - LED
#define LED3 D6        // Chân GPIO12 (D6) - Air Conditioner

#define ADC_REF_VOLTAGE 3.3      // Điện áp tham chiếu của ADC (3.3V)
#define REF_RESISTANCE 5000.0    // Điện trở tham chiếu của điện trở phân áp (5kΩ)
#define LUX_CALC_SCALAR 12518931 // Hằng số tính toán lux
#define LUX_CALC_EXPONENT -1.405 // Số mũ tính toán lux

// Thông tin kết nối WiFi và MQTT Broker
const char* ssid = "TOTOLINK_N100RE";             // Thay bằng SSID của bạn
const char* password = "0123456789";
const char* mqtt_server = "5268996a427b4742bc30f7ec3ea264ee.s1.eu.hivemq.cloud";
const char* mqtt_user = "nguyenhaiyen";
const char* mqtt_pass = "B21dccn129@";
const int mqtt_port = 8883;

const char* topic_light = "B21DCCN129/esp8266/light";
const char* topic_fan = "B21DCCN129/esp8266/fan";
const char* topic_airconditioner = "B21DCCN129/esp8266/airconditioner";

const char* topic_light_status = "B21DCCN129/esp8266/light/status";
const char* topic_fan_status = "B21DCCN129/esp8266/fan/status";
const char* topic_airconditioner_status = "B21DCCN129/esp8266/airconditioner/status";
const char* topic_sensor_data = "B21DCCN129/esp8266/sensor_data";

// Tạo đối tượng DHT và PubSubClient
DHT dht(DHTPIN, DHTTYPE);
WiFiClientSecure espClient;
PubSubClient client(espClient);
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", 7*3600, 60000);

// Hàm kết nối WiFi
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

// Hàm callback xử lý khi nhận tin nhắn từ MQTT
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.println("Callback function called");
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  // Chuyển payload thành chuỗi để so sánh
  String message = "";
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }

  // Xử lý bật/tắt LED dựa trên tin nhắn nhận được
  String ledState = (message == "ON") ? "ON" : "OFF";

  // Tạo một đối tượng JSON để gửi trạng thái
  StaticJsonDocument<200> doc;
  doc["state"] = ledState;

  char jsonBuffer[512];
  serializeJson(doc, jsonBuffer);

  if (strcmp(topic, topic_fan) == 0) {
    Serial.print("Light "); 
    digitalWrite(LED1, (ledState == "ON") ? HIGH : LOW);
    client.publish(topic_fan_status, jsonBuffer);

  } else if (strcmp(topic, topic_light) == 0) {
    Serial.print("fan");
    digitalWrite(LED2, (ledState == "ON") ? HIGH : LOW);
    client.publish(topic_light_status, jsonBuffer);

  } else if (strcmp(topic, topic_airconditioner) == 0) {
    Serial.print("airconditioner");
    digitalWrite(LED3, (ledState == "ON") ? HIGH : LOW);
    client.publish(topic_airconditioner_status, jsonBuffer);

  }
    Serial.print(ledState);
}

// Hàm kết nối lại nếu mất kết nối với MQTT Broker
void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientID =  "ESPClient-";
    clientID += String(random(0xffff),HEX);
    if (client.connect(clientID.c_str(), mqtt_user, mqtt_pass)) {
      Serial.println("connected");
      if (client.subscribe(topic_light)) {
        Serial.println("Subscribed to topic_light");
      }
      if (client.subscribe(topic_fan)) {
        Serial.println("Subscribed to topic_fan");
      }
      if (client.subscribe(topic_airconditioner)) {
        Serial.println("Subscribed to topic_airconditioner");
      }
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(LED1, OUTPUT);
  pinMode(LED2, OUTPUT);
  pinMode(LED3, OUTPUT);
  pinMode(LDR_PIN, INPUT);

  setup_wifi();
  espClient.setInsecure();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);

  dht.begin();
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // Đọc dữ liệu từ DHT11
  float h = dht.readHumidity();
  float t = dht.readTemperature(); // Nhiệt độ theo Celsius

  // Đọc giá trị từ cảm biến ánh sáng
  int rawData = analogRead(LDR_PIN);

  // Tính điện áp từ giá trị ADC
  float ldrVoltage = rawData * (ADC_REF_VOLTAGE / 1023.0);

  // Điện trở của LDR (với điện trở phân áp cố định)
  float resistorVoltage = ADC_REF_VOLTAGE - ldrVoltage;
  float ldrResistance = (ldrVoltage / resistorVoltage) * REF_RESISTANCE;

  // Tính giá trị lux
  float lux = LUX_CALC_SCALAR * pow(ldrResistance, LUX_CALC_EXPONENT);




  Serial.print("Humidity: ");
  Serial.print(h); 
  Serial.print(" %\t");
  Serial.print("Temperature: ");
  Serial.print(t); 
  Serial.print(" *C\t");
  Serial.print("Light: ");
  Serial.println(lux); 

  timeClient.update();
  unsigned long currentTime = timeClient.getEpochTime();

  // Tạo chuỗi JSON
  StaticJsonDocument<200> doc;
  doc["temperature"] = t;
  doc["humidity"] = h;
  doc["light"] = lux;
  doc["timestamp"] = currentTime; 
  char jsonBuffer[512];
  serializeJson(doc, jsonBuffer);

  // Gửi chuỗi JSON qua MQTT
  client.publish(topic_sensor_data, jsonBuffer);

  delay(2000); // Đợi 2 giây trước khi lặp lại
}







