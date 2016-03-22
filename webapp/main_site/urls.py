from django.conf.urls import url

from main_site import views

urlpatterns = [
    url(r'^$', views.home, name='home'),
    url(r'^test$', views.test, name='test'),
    url(r'^add_device$',views.add_device, name='add_device'),
    url(r'^add_log$', views.add_log, name='add_log'),
    url(r'^add_sensor$', views.add_sensor, name='add_sensor'),
]
