using Maui_app.API;

namespace Maui_app.Views;

public partial class DeviceControlPage : ContentPage
{
    DeviceAPI deviceAPI = new DeviceAPI("192.168.4.1");
	public DeviceControlPage()
	{
		InitializeComponent();
	}

    private void ButOnClick(object sender, EventArgs e)
    {
        deviceAPI.PostLed(new PinState(true));
    }

    private void ButOffClick(object sender, EventArgs e)
    {
        deviceAPI.PostLed(new PinState(false));
    }
}