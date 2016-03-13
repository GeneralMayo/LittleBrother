from __future__ import unicode_literals

from django.db import models

#Little Brother Devices
class Device(models.Model):
	id = models.IntegerField(primary_key=True)
	name = models.CharField(max_length=30)
	latitude = models.DecimalField(max_digits=9,decimal_places=7)
	longitude = models.DecimalField(max_digits=10,decimal_places=7)
	time_server = models.DateTimeField()

	def __unicode__(self):
		return "Device: %d %s %s %s" % (self.id, self.name,
					str(self.latitude), str(self.longitude))

	def __str__(self):
		return self.__unicode__()
#Sensors
#Sensor models are unique to each device. For example, two different devices
#that each have a temperature sensor will generate unique temperature sensor
#models.
class Sensor(models.Model):
	id = models.IntegerField(primary_key=True)
	name = models.CharField(max_length=30)
	device = models.ForeignKey(Device)
	time_server = models.DateTimeField()

	def __unicode__(self):
		return "Sensor: %d %s %d" % (self.id, self.name, self.device.id)

	def __str__(self):
		return self.__unicode__()

#Logs
class Log(models.Model):
	id = models.IntegerField(primary_key=True)
	time = models.DateTimeField()
	value = models.IntegerField()
	sensor = models.ForeignKey(Sensor)
	time_app = models.DateTimeField()  #time app received log
        time_server = models.DateTimeField()  #time server recieved log

	def __unicode__(self):
		return "Log: %d %s %d %d" % (self.id, str(self.time), self.value,
					self.sensor.id)
	
	def __str__(self):
		return self.__unicode__()
