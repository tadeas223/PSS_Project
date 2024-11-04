using Maui_app.Views;

namespace Maui_app
{
    public partial class AppShell : Shell
    {
        public AppShell()
        {
            InitializeComponent();

            Routing.RegisterRoute(nameof(DeviceControlPage), typeof(DeviceControlPage));
        }
    }
}
