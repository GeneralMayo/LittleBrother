from __future__ import unicode_literals

from django.db import models
from django.conf import settings
from django.contrib.auth.models import User

#Little Brother Devices
class Device(models.Model):
	name = models.CharField(max_length=8)
	latitude = models.FloatField()
	longitude = models.FloatField()
	time_server = models.DateTimeField()
        admin = models.ForeignKey(settings.AUTH_USER_MODEL,blank=True,null=True)
	config_revision = models.IntegerField(default=0)
	generating_logs = models.BooleanField(blank=True)

	def __unicode__(self):
		return "Device: %d %s %s %s" % (self.id, self.name,
					str(self.latitude), str(self.longitude))

	def __str__(self):
		return self.__unicode__()

	class Meta:
		ordering = ['id','name','time_server']

#Sensors
#Sensor models are unique to each device. For example, two different devices
#that each have a temperature sensor will generate unique temperature sensor
#models.
class Sensor(models.Model):
	custom_id = models.IntegerField(default=0)
	name = models.CharField(max_length=8)
	device = models.ForeignKey(Device)
	time_server = models.DateTimeField()

	def __unicode__(self):
		return "Sensor: %d %s %d" % (self.custom_id, self.name, self.device.id)

	def __str__(self):
		return self.__unicode__()

	class Meta:
		ordering = ['device','custom_id','name','time_server']

#Logs
class Log(models.Model):
	custom_id = models.IntegerField(default=0)
	time = models.DateTimeField()
	value = models.IntegerField()
	sensor = models.ForeignKey(Sensor)
	time_app = models.DateTimeField()  #time app received log
    	time_server = models.DateTimeField()  #time server recieved log

	def __unicode__(self):
		return "Log: %d %s %d %d" % (self.id, str(self.time), self.value,
					self.sensor.custom_id)
	
	def __str__(self):
		return self.__unicode__()

	class Meta:
		ordering = ['sensor','time','time_app','time_server']

class MyUser(models.Model):
	user = models.OneToOneField(settings.AUTH_USER_MODEL,on_delete=models.CASCADE)
