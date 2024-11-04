using Maui_app.Views;

namespace Maui_app
{
    public partial class MainPage : ContentPage
    {

        public MainPage()
        {
            InitializeComponent();
        }

        private void OnButtonClick(object sender, EventArgs e)
        {
            Shell.Current.GoToAsync(nameof(DeviceControlPage));
        }
    }

}
