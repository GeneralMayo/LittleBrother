from django import forms

from models import *

#Form for device creation
class DeviceForm(forms.ModelForm):

    class Meta:
        model = Device
        exclude = ('time_server',)

    def clean_latitude(self):
        latitude = float(self.cleaned_data.get('latitude'))
        if (latitude > 90.0 or latitude < -90.0):
            raise forms.ValidationError('Latitude must be between -90 and 90')
        return latitude

    def clean_longitude(self):
        longitude = float(self.cleaned_data.get('longitude'))
        if (longitude > 180.0 or longitude < -180.0):
            raise forms.ValidationError('Longitude must be between -180 and 180')
        return longitude

#form for sensor creation
class SensorForm(forms.ModelForm):
    class Meta:
        model = Sensor
        exclude = ('time_server',)


#form for log creation
class LogForm(forms.ModelForm):
    class Meta:
        model = Log
        exclude = ('time_server',)
