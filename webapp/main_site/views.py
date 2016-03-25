from django.shortcuts import render, redirect
from django.http import HttpResponse,HttpResponseBadRequest
from django.core.urlresolvers import reverse
from django.db import transaction
from django.shortcuts import get_object_or_404
from django.views.decorators.csrf import csrf_exempt

from models import *
from forms import *
from datetime import datetime

def home(request):
    context = {}
    context['devices'] = Device.objects.all()
    context['sensors'] = Sensor.objects.all()
    context['logs'] = Log.objects.all()
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
    form = DeviceForm(request.POST)

    if not form.is_valid():
        return HttpResponseBadRequest('Device parameters invalid')
    
    new_device = Device(id=form.cleaned_data['id'],
                        name=form.cleaned_data['name'],
                        latitude=form.cleaned_data['latitude'],
                        longitude=form.cleaned_data['longitude'],
                        time_server=datetime.now())
    new_device.save()
    return HttpResponse('Device saved')

@csrf_exempt
@transaction.atomic
def add_sensor(request):

    if 'device' not in request.POST:
        return HttpResponseBadRequest('Device id required')

    device = get_object_or_404(Device,pk=request.POST['device'])

    form = SensorForm(request.POST)

    if not form.is_valid():
        return HttpResponseBadRequest('Sensor parameters invalid')
    

    new_sensor = Sensor(custom_id=form.cleaned_data['custom_id'],
                        name=form.cleaned_data['name'],
                        time_server=datetime.now(),
                        device=device)
    new_sensor.save()
    return HttpResponse('Sensor saved')

@csrf_exempt
@transaction.atomic
def add_log(request):

    if 'sensor' not in request.POST:
        return HttpResponseBadRequest('Sensor id required')

    if 'device' not in request.POST:
        return HttpResponseBadRequest('Device id required')

    device = get_object_or_404(Device,pk=request.POST['device'])
    sensor_set = Device.sensor_set.all()
    sensor = get_object_or_404(sensor_set,custom_id=request.POST['sensor'])

    form = LogForm(request.POST)

    if not form.is_valid():
        return HttpResponseBadRequest('Log parameters invalid')

    

    new_log = Log(id=form.cleaned_data['id'],
                        time_server=datetime.now(),
                        sensor=sensor,
                        time_app=form.cleaned_data['time_app'],
                        value=form.cleaned_data['value'],
                        time=form.cleaned_data['time'])
    new_log.save()
    return HttpResponse('Log saved')    
