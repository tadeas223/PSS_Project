#include <LittleFS.h>
#include <FS.h>
#include <ESP8266WebServer.h>
#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <ArduinoJson.h>

#define CONFIG_FILE "/config.json"
#define PIN 14
#define API_PORT 6660

ESP8266WebServer server(API_PORT);

struct Config {
  int mode;
  String name;
  String ap_ssid;
  String ap_passwd;
  String wifi_ssid;
  String wifi_passwd;

  Config(int Amode, String Aname, String Aap_ssid, String Aap_passwd, String Awifi_ssid, String Awifi_passwd) :
  mode(Amode), name(Aname), ap_ssid(Aap_ssid), ap_passwd(Aap_passwd), wifi_ssid(Awifi_ssid), wifi_passwd(Awifi_passwd) {}
};

// global config initialized to default
Config config = Config(
  0,
  "device",
  "Device-AP",
  "12345678",
  "",
  ""
);

template <typename T>
struct Result {
  bool ok;
  int code;
  T value;

  Result(int a, int b, T c) : ok(a), code(b), value(c) {}
  Result(int a, int b) : ok(a), code(b), value(value) {}

};

struct Response {
  bool send_body;
  JsonDocument json;
  String message;
  int status;

  Response(String msg, int stat) : send_body(false), message(msg), status(stat) {}
  Response(JsonDocument doc, int stat) : send_body(true), json(doc), status(stat) {}
};

///////////////////////////////////////////////
// Error handling
///////////////////////////////////////////////


template <typename T>
Result<T> result_error(int code) {
  return Result<T>(false, code);
}

template <typename T>
Result<T> result_ok(T value) {
  return Result(true, 0, value);
}

///////////////////////////////////////////////
// PIN stuff
///////////////////////////////////////////////
void pin_init() {
  print_info("pin", "initialized");
  pinMode(PIN, OUTPUT);
}

///////////////////////////////////////////////
// Logging functions
///////////////////////////////////////////////
void serial_init() {
  Serial.begin(115200);
  Serial.println("");
  print_info("Serial", "initialized");
}

String clamp_and_pad(String input, int maxLength) {
  if(input.length() > maxLength) {
    return input.substring(0, maxLength);
  } else {
    while(input.length() < maxLength) {
      input += ' ';
    }
    return input;
  }
}

void print_info(String topic, String msg) {
  topic = clamp_and_pad(topic, 15);
  Serial.printf("[INFO]    %s %s\n", topic.c_str(), msg.c_str());
}

void print_error(String topic, String msg) {
  topic = clamp_and_pad(topic, 15);
  Serial.printf("[ERROR]   %s %s\n", topic.c_str(), msg.c_str());
}

void print_warning(String topic, String msg) {
  topic = clamp_and_pad(topic, 15);
  Serial.printf("[WARNING] %s %s\n", topic.c_str(), msg.c_str());
}


///////////////////////////////////////////////
// LittleFS functions
///////////////////////////////////////////////
Result<bool> little_fs_init() {
  if(!LittleFS.begin()) {
    print_warning("LittleFS", "failed to initialize");
    return result_error<bool>(0);
  }
  print_info("LittleFS", "initialized");
  return result_ok<bool>(true);
}

Result<String> file_read(String path) {
  File file = LittleFS.open(path, "r");
  if(!file) {
    return result_error<String>(0);
  }
  String content = file.readString();
  file.close();
  return result_ok<String>(content);
}

Result<bool> file_write(String path, String data) {
  File file = LittleFS.open(path, "w");
  if(!file) {
      return result_error<bool>(0);
  }
  if(file.print(data)) {
    return result_ok<bool>(true);
  } else {
    return result_error<bool>(0);
  }
}

///////////////////////////////////////////////
// Network stuff
///////////////////////////////////////////////
Result<bool> wifi_init(String ssid, String passwd) {
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, passwd);

  print_info("NET--WiFi", "connecting...");
  int timeout = 20; // 10 seconds
  while(WiFi.status () != WL_CONNECTED) {
    if(timeout <= 0) {
      print_error("WiFi", "failed to initialize");
      return result_error<bool>(0);
    }
    delay(500);
    timeout--;
  }

  print_info("WiFi", "initialized");
  print_info("WiFi", WiFi.localIP().toString());
  return result_ok<bool>(true);
}

Result<bool> mdns_init(String name) {
  if(MDNS.begin(name)) {
    print_info("MDNS", "initialized");
    print_info("MDNS", name);
    return result_ok<bool>(true);
  } else {
    print_error("MDNS", "failed to initialize");
    return result_error<bool>(0);
  }
}

void ap_init(String ssid, String passwd) {
  WiFi.mode(WIFI_AP);
  WiFi.softAP(ssid, passwd);

  print_info("AP", "initialized");
  print_info("AP", WiFi.softAPIP().toString());
}

///////////////////////////////////////////////
// Configuration stuff
///////////////////////////////////////////////
Config config_from_json(JsonDocument doc) {
  return Config(
    doc["mode"],
    doc["name"],
    doc["ap_ssid"],
    doc["ap_passwd"],
    doc["wifi_ssid"],
    doc["wifi_passwd"]
  );
}

JsonDocument config_to_json(Config config) {
  DynamicJsonDocument doc(512);
  doc["mode"] = config.mode;
  doc["name"] = config.name;
  doc["ap_ssid"] = config.ap_ssid;
  doc["ap_passwd"] = config.ap_passwd;
  doc["wifi_ssid"] = config.wifi_ssid;
  doc["wifi_passwd"] = config.wifi_passwd;
  return doc;
}

Result<bool> config_write(Config config, String path) {
  JsonDocument doc = config_to_json(config);
  String jsonString;
  serializeJson(doc, jsonString);
  if(!file_write(path, jsonString).ok) {
    return result_error<bool>(0);
  }
  return result_ok<bool>(true);
}

Result<Config> config_read(String path) {

  Result<String> fileResult = file_read(path);
  if(!fileResult.ok) {
    print_warning("Config", String("failed to load config from ") + path);
    return result_error<Config>(0);
  }
  DynamicJsonDocument doc(512);
  deserializeJson(doc, fileResult.value);

  print_info("Config", String("config read from ") + path);
  return result_ok<Config>(config_from_json(doc));
}

///////////////////////////////////////////////
// Response functtions
///////////////////////////////////////////////
Result<JsonDocument> request_get() {
  String body = server.arg("plain");
  DynamicJsonDocument doc(512);
  DeserializationError error = deserializeJson(doc, body);
  if(error) {
      print_info("API", "failed to parse request body");
      return result_error<JsonDocument>(0);
  }
  print_info("API", "request body retrieved");
  print_info("API", body);
  return result_ok<JsonDocument>(doc);
}

Response response_create(String message, int status) {
  return Response(message, status);
}

Response response_create(JsonDocument json, int status) {
  return Response(json, status);
}

void response_send(Response response) {
  String jsonString;
  if(response.send_body) {
    serializeJson(response.json, jsonString);
  } else {
    DynamicJsonDocument doc(512);
    doc["message"] = response.message;
    serializeJson(doc, jsonString);
  }
  print_info("API", "sending response");
  print_info("API", String(response.status) + ": " + jsonString);
  server.send(response.status, "application/json", jsonString);
}

///////////////////////////////////////////////
// API server
///////////////////////////////////////////////

void server_routing() {
  server.onNotFound(api_not_found);

  server.on("/helloworld", HTTP_GET, api_get_hello_world);
  server.on("/pin", HTTP_POST, api_post_pin);
  server.on("/pin", HTTP_GET, api_get_pin);  
  server.on("/switch", HTTP_POST, api_post_switch);
  server.on("/config", HTTP_GET, api_get_config);
  server.on("/config", HTTP_POST, api_post_config);
  server.on("/reset", HTTP_POST, api_post_reset);

  print_info("API-server", "setting routes");
}

///////////////////////////////////////////////
// Request handlers
///////////////////////////////////////////////
void api_not_found() {
  print_info("API", "404 Not Found");

  Response res = response_create("Not Found", 404);
  response_send(res);
}

void api_get_hello_world() {
  print_info("API", "GET /helloworld");
  StaticJsonDocument<128> doc;
  doc["message"] = "Hello, World!";

  Response res = response_create(doc, 200);
  response_send(res);
}

void api_post_pin() {
  print_info("API", "POST /pin");

  Result<JsonDocument> req = request_get();
  if(!req.ok || !req.value.containsKey("value")) {
    Response err = response_create("Bad Request", 400);
    response_send(err);
    return;
  }

  JsonVariant valueVariant = req.value.as<JsonVariant>();
  bool value = valueVariant["value"].as<bool>();

  print_info("API", String("pin set to ") + (value)? "true" : "false");
  digitalWrite(PIN, !value);

  Response res = response_create("OK", 200);
  response_send(res);
}

void api_get_pin() {
  print_info("API", "GET /pin");

  StaticJsonDocument<128> doc;
  doc["value"] = digitalRead(PIN);

  Response res = response_create(doc, 200);
  response_send(res);
}

void api_post_switch() {
  print_info("API", "POST /switch");

  print_info("API", String("pin set to ") + ((!digitalRead(PIN))? "true" : "false"));
  digitalWrite(PIN, !digitalRead(PIN));

  Response res = response_create("OK", 200);
  response_send(res);
}

void api_get_config() {
  print_info("API", "GET /config");

  JsonDocument doc = config_to_json(config);

  Response res = response_create(doc, 200);
  response_send(res);
}

void api_post_config() {
  print_info("API", "POST /config");
  Response err = response_create("Bad Request", 400);

  Result<JsonDocument> req = request_get();
  if(!req.ok) {
    response_send(err);
    return;
  }

  if(!req.value.containsKey("mode") &&
  !req.value.containsKey("name") &&
  !req.value.containsKey("ap_ssid") &&
  !req.value.containsKey("ap_passwd") &&
  !req.value.containsKey("wifi_ssid") &&
  !req.value.containsKey("wifi_passwd")) {
    response_send(err);
    return;
  }

  Result<bool> res = config_write(config_from_json(req.value), CONFIG_FILE);
  if(!res.ok) {
    print_error("API", "failed to save config");
    response_send(err);
  }

  Response response = response_create("OK", 200);
  response_send(response);

  print_info("API", "restarting");
  ESP.restart();
}

void api_post_reset() {
  print_info("API", "POST reset");
  
  Response res = response_create("OK", 200);
  response_send(res);

  ESP.restart();
}

void setup() {
  serial_init();
  little_fs_init();
  pin_init();

  Result<Config> readConf = config_read(CONFIG_FILE);
  if(readConf.ok) {
    config = readConf.value;
  } else {
    print_warning("SETUP", "defalut config loaded");
  }

  String confJson;
  JsonDocument doc = config_to_json(config);
  serializeJson(doc, confJson);
  print_info("Config", confJson);

  if(config.mode == 0) {
    ap_init(config.ap_ssid, config.ap_passwd);
  } else {
    Result<bool> wifiRes = wifi_init(config.wifi_ssid, config.wifi_passwd);
    if(!wifiRes.ok) {
      print_error("SETUP", "WiFi failed to initialize");
      ESP.restart();
    }

    Result<bool> mdnsRes = mdns_init(config.name);
  }

  server_routing();

  server.begin();
  print_info("SETUP", "server running");
  print_info("SETUP", "setup complete");


}

void loop() {
  server.handleClient();
}
