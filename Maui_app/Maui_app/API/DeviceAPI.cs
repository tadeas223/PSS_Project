using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace Maui_app.API
{
    public class DeviceAPI
    {
        private static readonly HttpClient client = new HttpClient();

        public string IP;

        public DeviceAPI(string IP) { this.IP = IP; }
        public string GetHelloWorld()
        {
            var task = client.GetStringAsync(IP + "/get/helloworld");
            task.Wait();
            return task.Result;
        }

        public Config GetConfig()
        {
            var task = client.GetFromJsonAsync<Config>(IP + "/get/config");
            task.Wait();
            return task.Result;
        }

        public Message PostConfig(Config config)
        {
            var stringContent = new StringContent(JsonSerializer.Serialize(config));
            var task = client.PostAsync(IP + "/put/config", stringContent);
            task.Wait();
            var response = task.Result;
            var content = response.Content.ReadAsStringAsync();
            content.Wait();
            Message msg = JsonSerializer.Deserialize<Message>(content.Result);
            return msg;
        }

        public Message PostLed(PinState state)
        {
            var stringContent = new StringContent(JsonSerializer.Serialize(state));
            Console.WriteLine(stringContent);
            var task = client.PostAsync(IP + "/post/led", stringContent);
            task.Wait();
            var response = task.Result;
            var content = response.Content.ReadAsStringAsync();
            content.Wait();
            Message msg = JsonSerializer.Deserialize<Message>(content.Result);
            return msg;
        }


    }
}
