#include <Arduino.h>
#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <ESP8266WebServer.h>
#include <FS.h>
#include <LittleFS.h>
#include <variant>

#define DEFAULT_CONFIG "default_config.json"
#define CONFIG "config.json"

using namespace std;

void(* resetFunc) (void) = 0;

ESP8266WebServer server(80);

int mode = 0;
// 0 - controll trough AP
// 1 - controll trough a different wifi network
char* name = "device";
char* ap_ssid = "device-AP";
char* ap_passwd = "12345678";
char* wifi_ssid = NULL;
char* wifi_passwd = NULL;

void setup() {
  Serial.begin(115200);
  Serial.println("");

  pinMode(BUILTIN_LED, OUTPUT);

  if (!LittleFS.begin()) {
    Serial.println("SPIFFS failed");
  }

  Serial.print("name=");
  Serial.println(name);

  modeSetup();

  serverRouting();
  server.begin();
}

void loop() {
  server.handleClient();
}

void modeSetup() {
  Serial.println("Mode setup");
  Serial.print("mode=");
  Serial.println(mode);

  if (mode == 0) {
    APsetup();
  } else if (mode == 1) {
    if (!WifiSetup()) {
      APsetup();
    }
  }
}

void APsetup() {
  WiFi.mode(WIFI_AP);
  WiFi.softAP(ap_ssid, ap_passwd);

  Serial.println("AP started");

  Serial.print("ap_ssid=");
  Serial.println(ap_ssid);

  Serial.print("ap_passwd=");
  Serial.println(ap_passwd);
}

bool WifiSetup() {
  if (wifi_ssid == NULL || wifi_passwd == NULL) {
    Serial.println("Wifi setup failed, missing credentials");
    return false;
  }

  WiFi.mode(WIFI_STA);
  WiFi.begin(wifi_ssid, wifi_passwd);

  Serial.print("wifi_ssid=");
  Serial.println(wifi_ssid);

  Serial.print("wifi_passwd=");
  Serial.println(wifi_passwd);

  Serial.print("Connecting");

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("Connected");

  Serial.print("local ip=");
  Serial.println(WiFi.localIP());

  if (MDNS.begin(name)) {
    Serial.println("MDNS responder started");
    Serial.println(strcat("MDNS name = ", name));
  }

  return true;
}

void serverRouting() {
  server.onNotFound(handleNotFound);

  server.on("/get/helloworld", HTTP_GET, getHelloWorld);
  server.on("/get/helloworldjson", HTTP_GET, getHelloWorldJson);
  server.on("/get/config", HTTP_GET, getConfig);
  server.on("/post/led", HTTP_POST, postLed);
  server.on("/post/config", HTTP_POST, postConfig);
}

variant<JsonDocument, bool> getJson() {
  String body = server.arg("plain");
  DynamicJsonDocument doc(512);
  DeserializationError error = deserializeJson(doc, body);

  if (error) {
    DynamicJsonDocument errDoc(512);
    errDoc["status"] = "ERROR";
    errDoc["message"] = "Unnable to format json body";

    String buf;
    serializeJson(errDoc, buf);

    server.send(400, "application/json", buf);
    return true;
  }

  return doc;
}

// HTTP server handlers

void handleNotFound() {
  server.send(404, "text/plain", "404 Not Found :(");
}

void getHelloWorld() {
  server.send(200, "text/plain", "Hello World!");
}

void getHelloWorldJson() {
  StaticJsonDocument<32> doc;
  doc["greeting"] = "Hello World!";
  String json;
  serializeJson(doc, json);

  server.send(200, "application/json", json);
}

void postConfig() {
  variant variant = getJson();
  if (holds_alternative<bool>(variant)) {
    return;
  }

  JsonDocument doc = get<JsonDocument>(variant);
  JsonVariant jsonVariant = doc.as<JsonVariant>();

  if (!jsonVariant.containsKey("name") || 
  !jsonVariant.containsKey("mode") ||
  !jsonVariant.containsKey("ap_ssid") || 
  !jsonVariant.containsKey("ap_passwd") || 
  !jsonVariant.containsKey("wifi_ssid") || 
  !jsonVariant.containsKey("wifi_passwd")) {
    DynamicJsonDocument errDoc(512);
    errDoc["status"] = "ERROR";
    errDoc["message"] = "Missing variables";

    String buf;
    serializeJson(errDoc, buf);

    server.send(400, "application/json", buf);
  }

  String configString;
  deserializeJson(doc, configString);

  File configFile = LittleFS.open(CONFIG, "w");

  if(!configFile) {
    Serial.println("Failed to open config file for writing");
    DynamicJsonDocument errDoc(512);
    errDoc["status"] = "ERROR";
    errDoc["message"] = "missing credentials";

    String buf;
    serializeJson(errDoc, buf);

    server.send(400, "application/json", buf);
    return;
  }

  String buf;
  serializeJson(doc, buf);
  configFile.print(buf);
  configFile.close();

  DynamicJsonDocument responseDoc(512);
  responseDoc["status"] = "OK";
  responseDoc["message"] = "Config saved. Rebooting device!!!";

  buf = "";
  serializeJson(responseDoc, buf);

  server.send(400, "application/json", buf);

  resetFunc();
}

void getConfig() {
  File configFile = LittleFS.open(CONFIG, "r");

  if(!configFile) {
    Serial.println("Failed to open config file for writing");
    DynamicJsonDocument errDoc(512);
    errDoc["status"] = "ERROR";
    errDoc["message"] = "missing credentials";

    String buf;
    serializeJson(errDoc, buf);

    server.send(400, "application/json", buf);
    return;
  }

  String configStr = "";
  while(configFile.available()){
    configStr.concat(configFile.read());  
  }
  configFile.close();

  server.send(200, "application/json", configStr);
}

void postLed() {
  variant variant = getJson();
  if (holds_alternative<bool>(variant)) {
    return;
  }

  JsonDocument doc = get<JsonDocument>(variant);
  JsonVariant jsonVariant = doc.as<JsonVariant>();

  if(jsonVariant.containsKey("state")) {
    const char* state = jsonVariant["state"];
    if(strcmp(state, "on")) {
      digitalWrite(BUILTIN_LED, HIGH);
    } else {
      digitalWrite(BUILTIN_LED, LOW);
    }
  }

  DynamicJsonDocument errDoc(512);
  errDoc["status"] = "ERROR";
  errDoc["message"] = "missing variable";

  String buf;
  serializeJson(errDoc, buf);

  server.send(400, "application/json", buf);
}