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

int pin = BUILTIN_LED;

struct Response {
  const char* message;
  const char* status;
  int statusCode;
};
struct Configuration {
  int mode;
  // 0 - controll trough AP
  // 1 - controll trough a different wifi network
  const char* name;
  const char* ap_ssid;
  const char* ap_passwd;
  const char* wifi_ssid;
  const char* wifi_passwd;
};

Configuration configuration;

void setup() {
  Serial.begin(115200);
  Serial.println("");

  loadDefaultConfig();
  serverRouting();
}

void loop() {
  server.handleClient();
}

//
// Wifi stuff
//
void modeSetup() {
  if (configuration.mode == 0) {
    APsetup();
  } else if (configuration.mode == 1) {
    if (!WifiSetup()) {
      APsetup();
    }
  }
}

void APsetup() {
  WiFi.mode(WIFI_AP);
  WiFi.softAP(configuration.ap_ssid, configuration.ap_passwd);
}

bool WifiSetup() {
  if (configuration.wifi_ssid == NULL || configuration.wifi_passwd == NULL) {
    return false;
  }

  WiFi.mode(WIFI_STA);
  WiFi.begin(configuration.wifi_ssid, configuration.wifi_passwd);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  if (MDNS.begin(configuration.name)) {
    // TODO: log something to serial
  }

  return true;
}

//
// Configuration stuff
//
void loadDefaultConfig() {
  configuration.mode = 0;
  configuration.name = "device";
  configuration.ap_ssid = "device-AP";
  configuration.ap_passwd = "12345678";
  configuration.wifi_ssid = NULL;
  configuration.wifi_passwd = NULL;
}

void serializeConfiguration(Configuration* config, JsonDocument* doc) {
  (*doc)["mode"] = config->mode;
  (*doc)["name"] = config->name;
  (*doc)["ap_ssid"] = config->ap_ssid;
  (*doc)["ap_passwd"] = config->ap_passwd;
  (*doc)["wifi_ssid"] = config->wifi_ssid;
  (*doc)["wifi_passwd"] = config->wifi_passwd;
}

void deserializeConfig(JsonDocument* doc, Configuration* config) {
  config->mode = (*doc)["mode"];
  config->name = (*doc)["name"];
  config->ap_ssid = (*doc)["ap_ssid"];
  config->ap_passwd = (*doc)["ap_passwd"];
  config->wifi_ssid = (*doc)["wifi_ssid"];
  config->wifi_passwd = (*doc)["wifi_passwd"];
}

//
// API stuff
//

void serverRouting() {
  server.onNotFound(handleNotFound);

  server.on("/helloworld", HTTP_GET, getHelloWorld);
  server.on("/control/value", HTTP_POST, postControlValue);
}

//
// Additional Methods for HTTP
//

void generateResponseJson(Response response, JsonDocument* doc) {
  (*doc)["message"] = response.message;
  (*doc)["status"] = response.status;
}

void sendResponse(Response response) {
  StaticJsonDocument<256> doc;
  generateResponseJson(response, &doc);

  String jsonString;
  serializeJson(doc, jsonString);
  server.send(response.statusCode, "application/json", jsonString);
}

// Retrieves a JSON body from a server request.
// The value false is returned when an error occured while parsing the JSON.
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

//
//Response builders
//

// Response that represents missing or invalid values passed in the request body.
// Or if an invalid JSON was recieved.
Response BadRequestResponse(const char* message) {
  Response response;
  response.status = "Bad Request";
  response.statusCode = 400;
  response.message = message;
  return response;
}

Response SuccessResponse(const char* message) {
  Response response;
  response.status = "OK";
  response.statusCode = 200;
  response.message = message;
  return response;
}

//
// HTTP handlers
//

void handleNotFound() {
  Response response;
  response.message = "Not Found";
  response.status = "ERROR";
  response.statusCode = 404;

  sendResponse(response);
}

void getHelloWorld() {
  StaticJsonDocument<128> doc;
  doc["message"] = "Hello, World!";

  String jsonString;
  serializeJson(doc, jsonString);

  server.send(200, "application/json", jsonString);
}

// Sets a desired pin HIGH or LOW based on the JSON request.
// The pin that is controlled is set in the "pin" variable
// JSON arguments:
//     - bool value    Represents the value of the pin (true = HIGH, false = LOW)
void postControlValue() {
  variant variant = getJson();
  if (holds_alternative<bool>(variant)) {
    return;
  } else {
    Response response = BadRequestResponse("Failed parsing request body into JSON");
    sendResponse(response);
    return;
  }

  JsonDocument doc = get<JsonDocument>(variant);
  
  if(!doc.containsKey("value")) {
    Response response = BadRequestResponse("Missing argument value.");
    sendResponse(response);
    return;
  }

  JsonVariant jsonVariant = doc.as<JsonVariant>();
  bool value = jsonVariant["value"].as<bool>();

  if(value) {
    digitalWrite(pin, HIGH);
  } else {
    digitalWrite(pin, LOW);
  }

  Response response = SuccessResponse("Pin value set");
  sendResponse(response);
}

























