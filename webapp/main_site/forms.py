from django import forms

from models import *
from django.core.validators import validate_email
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

    def clean_admin(self):
	admin_name = self.cleaned_data['admin']
	if not User.objects.filter(username=admin_name).exists():
	    raise forms.ValidationError('Admin username does not exist')
	return admin_name
	

#form for sensor creation
class SensorForm(forms.ModelForm):
    class Meta:
        model = Sensor
        exclude = ('time_server',)


#form for log creation
class LogForm(forms.ModelForm):
    device = forms.IntegerField()
    sensor_id = forms.IntegerField()

    class Meta:
        model = Log
        exclude = ('time_server','sensor')

#form to register for the site
class RegistrationForm(forms.Form):
    username = forms.CharField(max_length = 20)
    password1 = forms.CharField(max_length = 200, 
                                label='Password', 
                                widget = forms.PasswordInput())
    password2 = forms.CharField(max_length = 200, 
                                label='Confirm password',  
                                widget = forms.PasswordInput())
    email = forms.CharField(max_length = 40,
                                 validators = [validate_email])


    def clean(self):
        cleaned_data = super(RegistrationForm, self).clean()

        password1 = cleaned_data.get('password1')
        password2 = cleaned_data.get('password2')
        if password1 and password2 and password1 != password2:
            raise forms.ValidationError("Passwords did not match.")

        return cleaned_data

    def clean_username(self):
        username = self.cleaned_data['username']
        if User.objects.filter(username=username).exists():
            raise forms.ValidationError("Username already exists")
        return username
