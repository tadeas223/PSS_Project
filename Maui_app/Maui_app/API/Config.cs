using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Maui_app.API
{
    public struct Config
    {
        public int mode { get; set; }
        public string name { get; set; }
        public string ap_ssid { get; set; }
        public string ap_passwd { get; set; }
        public string wifi_ssid { get; set; }
        public string wifi_passwd { get; set; }
    }
}
