from django.shortcuts import render, redirect
from django.http import HttpResponse
from django.core.urlresolvers import reverse

def home(request):
    return HttpResponse("Hello World")

def home_redirect(request):
    return redirect(reverse('home'))
