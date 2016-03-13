from django.conf.urls import url

from main_site import views

urlpatterns = [
    url(r'^$', views.home, name='home'),

]
