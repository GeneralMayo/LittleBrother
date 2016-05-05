from django.shortcuts import render, redirect
from django.http import HttpResponse,HttpResponseBadRequest,JsonResponse
from django.core.urlresolvers import reverse
from django.db import transaction
from django.shortcuts import get_object_or_404
from django.views.decorators.csrf import csrf_exempt
import django.core.serializers

from django.contrib.auth.decorators import login_required
from models import *
from forms import *
from datetime import timedelta
from datetime import datetime

from utils import write_log
import sys
import time

def test_graph(request):
    context = {}
    logs = Log.objects.all()

    for log in logs:
        log.time = log.time.strftime("%Y-%m-%d %H:%M:%S");
    context['logs'] = logs

    return render(request, 'test_graph.html', context)

def add_many_logs(request):
    Log.objects.all().delete()
    sensor = Sensor.objects.all()[0]
    dt = datetime.now()
    for i in xrange(0, 15):
        log =  Log(custom_id=i,
                   time_server=datetime.now(),
                   sensor=sensor,
                   time_app=datetime.now(),
                   value=i,
                   time=(dt + timedelta(minutes=i)))
        log.save()

    sensor = Sensor.objects.all()[1]
    dt = datetime.now()
    for i in xrange(0, 15):
        log =  Log(custom_id=i,
                   time_server=datetime.now(),
                   sensor=sensor,
                   time_app=datetime.now(),
                   value=(i + 2*i**2),
                   time=(dt + timedelta(minutes=i)))
        log.save()

    sensor = Sensor.objects.all()[2]
    dt = datetime.now()
    for i in xrange(0, 15):
        log =  Log(custom_id=i,
                   time_server=datetime.now(),
                   sensor=sensor,
                   time_app=datetime.now(),
                   value=(5*i + i**2),
                   time=(dt + timedelta(minutes=i)))
        log.save()

    return JsonResponse({'sucess' : 'Logs saved'})


def home(request):
    context = {}
    context['devices'] = Device.objects.all()
    return render(request,'home.html',context)

def device_info(request):
    context = {}
    context['devices'] = Device.objects.all()
    context['sensors'] = Sensor.objects.all()
    context['logs']    = Log.objects.all()
    return render(request,'device_info.html',context)

def home_redirect(request):
    return redirect(reverse('home'))

def test(request):
    context = {}
    context['device_form'] = DeviceForm()
    context['sensor_form'] = SensorForm()
    context['log_form'] = LogForm()
    return render(request,'test.html',context)

@csrf_exempt
@transaction.atomic
def add_device(request):
    write_log("Adding new device")
    form = DeviceForm(request.POST)

    if not form.is_valid():
        write_log("Device parameters invalid: " + str(form.errors.as_data()))
        return HttpResponseBadRequest('Device parameters invalid' + \
				      str(form.errors.as_data()))
    if form.cleaned_data['admin']:
    	new_device = Device(name=form.cleaned_data['name'],
                        latitude=form.cleaned_data['latitude'],
                        longitude=form.cleaned_data['longitude'],
                        time_server=datetime.now(),
			admin=User.objects.get(username=form.cleaned_data['admin']),
                        config_revision=0,
                        generating_logs=True)
    else:
        new_device = Device(name=form.cleaned_data['name'],
                        latitude=form.cleaned_data['latitude'],
                        longitude=form.cleaned_data['longitude'],
                        time_server=datetime.now(),
                        admin=User.objects.get(username="all_users"),
                        config_revision=0,
                        generating_logs=True)
    new_device.save()
    write_log("New device saved")
    return JsonResponse({'id' : new_device.id,
                         'sucess': 'Device saved'})

@csrf_exempt
@transaction.atomic
def add_sensor(request):
    write_log("Adding new sensor")
    if 'device' not in request.POST:
        write_log("Device id not included in POST")
        return HttpResponseBadRequest('Device id required')

    device = get_object_or_404(Device,pk=request.POST['device'])

    form = SensorForm(request.POST)

    if not form.is_valid():
        write_log("Sensor paramters invalid: " + str(form.errors.as_data()))
        return HttpResponseBadRequest('Sensor parameters invalid')
    
    other_sensors = device.sensor_set.filter(custom_id=form.cleaned_data['custom_id'])
    if len(other_sensors) != 0:
	return HttpResponseBadRequest('Sensor custom id taken')
    
    new_sensor = Sensor(custom_id=form.cleaned_data['custom_id'],
                        name=form.cleaned_data['name'],
                        time_server=datetime.now(),
                        device=device)
    new_sensor.save()
    write_log("New sensor saved")
    return JsonResponse({'success' : 'Sensor saved'})

@csrf_exempt
@transaction.atomic
def add_log(request):
    write_log('Adding new log')
    if 'sensor_id' not in request.POST:
        write_log('Sensor id not in POST')
        return HttpResponseBadRequest('Sensor id required')

    if 'device' not in request.POST:
        write_log('Device id not in post')
        return HttpResponseBadRequest('Device id required')

    device = get_object_or_404(Device,pk=request.POST['device'])
    sensor_set = device.sensor_set.all()
    sensor = get_object_or_404(sensor_set,custom_id=request.POST['sensor_id'])

    form = LogForm(request.POST)

    if not form.is_valid():
        write_log('Log parameters invalid: ' + str(form.errors.as_data()))
        return HttpResponseBadRequest('Log parameters invalid')

    other_logs = sensor.log_set.filter(custom_id=form.cleaned_data['custom_id'])

    if len(other_logs) != 0:
        return HttpResponseBadRequest('Log custom id already added')

    new_log = Log(custom_id=form.cleaned_data['custom_id'],
                        time_server=datetime.now(),
                        sensor=sensor,
                        time_app=form.cleaned_data['time_app'],
                        value=form.cleaned_data['value'],
                        time=form.cleaned_data['time'])
    new_log.save()
    write_log('New log saved')
    return JsonResponse({'success' : 'Log saved'})

@transaction.atomic
def delete_all_logs(request):
    Log.objects.all().delete()
    write_log("Deleted all Logs")
    return JsonResponse({'success' : 'All logs deleted'})

@transaction.atomic
def delete_device_logs(request):
    if 'device' not in request.POST:
        write_log('delete_device_logs: device id not in post')
        return HttpResponseBadRequest('Device id required')
    
    device = get_object_or_404(Device,pk=request.POST['device'])
    logs = Log.objects.filter(device=device)
    logs.delete()
    return home(request)

@transaction.atomic
def device_data(request, device_id):
    device = Device.objects.get(id=device_id)
    sensors = Sensor.objects.filter(device=device)
    logs = Log.objects.none()

    for sensor in sensors:
        sensor_logs = Log.objects.filter(sensor=sensor)
        for log in sensor_logs:
            log.datetime = log.time.strftime("%Y-%m-%d %H:%M:%S");
        sensor.sensor_logs = sensor_logs

    context = {}
    context['device'] = device
    context['sensors'] = sensors
    return render(request,'device_data.html',context)

def test_chart(request):
    context = {}
    context["logs"] = Log.objects.all()
    context["devices"] = Device.objects.all()
    return render(request,'test_chart.html', context)

@transaction.atomic
def register(request):
    context = {}

    if request.method == 'GET':
        context['form'] = RegistrationForm()
        return render(request, 'register.html', context)

    form = RegistrationForm(request.POST)
    context['form'] = form

    if not form.is_valid():
        return render(request, 'register.html', context)

    new_user = User.objects.create_user(username=form.cleaned_data['username'], 
                                        password=form.cleaned_data['password1'],
                                        email=form.cleaned_data['email']
                                        )
    myuser = MyUser(user=new_user)
    myuser.save()
    new_user.save()
    return redirect(reverse('home'))

@transaction.atomic
@login_required
def configure(request,id):
    context = {}

    try:
        device = Device.objects.get(id=id)
    except Device.DoesNotExist:
        return JsonResponse({"error" : "Device with id %s not found" % (id)})

    context["device"] = device
    if request.method == 'GET':
        context['form'] = ConfigurationForm()
        return render(request, 'configuration.html', context)

    form = ConfigurationForm(request.POST)
    context['form'] = form

    if not request.user == device.admin:
        context['errors'] = ["You are not an admin for device %s" % (id)]
        return render(request,'configuration.html',context)

    if not form.is_valid():
        return render(request, 'configuration.html', context)

    if (form.cleaned_data['name']):
        device.name = form.cleaned_data['name']

    if (form.cleaned_data['longitude']):
        device.longitude = form.cleaned_data['longitude']

    if (form.cleaned_data['latitude']):
        device.latitude = form.cleaned_data['latitude']

    if (form.cleaned_data['generating_logs']):
        value = form.cleaned_data['generating_logs'].lower()
        if (value == "true"):
            device.generating_logs = True
        elif (value == "false"):
            device.generating_logs = False 

    device.config_revision = device.config_revision + 1
    device.save()         
    context['success'] = "Configuration completed successfully"
    return render(request,'configuration.html',context)

@transaction.atomic
@csrf_exempt
def download_config(request,id):
    context = {}

    if "id" not in request.POST:
        return JsonResponse({"error" : "configuration id not found"})
	
    current_config = int(request.POST["id"])
    
    try:
        device = Device.objects.get(id=id)
        latest_config = device.config_revision
    except Device.DoesNotExist:
        return JsonResponse({"error": "Device with id %d not found" % (id)})

    if (latest_config > current_config):
        response = django.core.serializers.serialize('json',[device])
        response.strip('[]')
        return JsonResponse(response,safe=False)
    elif latest_config == current_config:
        return JsonResponse({"success" : "You already have the latest config"})
    else:
        return JsonResponse({"error" : "Supplied config is newer than latest config"})

@login_required
def your_devices(request):
    context = {}
    context["devices"] = Device.objects.filter(admin=request.user)
    return render(request,"device_info.html",context)
