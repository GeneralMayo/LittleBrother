from django.conf.urls import url
import django.contrib.auth.views as auth_views
from main_site import views

urlpatterns = [
    url(r'^$', views.home, name='home'),
    url(r'^test$', views.test, name='test'),
    url(r'^add_device$',views.add_device, name='add_device'),
    url(r'^add_log$', views.add_log, name='add_log'),
    url(r'^add_sensor$', views.add_sensor, name='add_sensor'),
    url(r'^delete_all_logs$', views.delete_all_logs, name='delete_all_logs'),
    url(r'^device_data/(?P<device_id>\d+)$', views.device_data, name='device_data'),
    url(r'^test_chart$', views.test_chart, name='test_chart'),
    url(r'^login$',auth_views.login, {'template_name':'login.html'}, name='login'),
    url(r'^logout$',auth_views.logout_then_login, name='logout'),
    url(r'^register$',views.register, name='register'),
    url(r'configure$',views.configure,name='configure'),
    url(r'download_config/(?P<device_id>\d+)',views.download_config,name='download_config'),
]
