# -*- coding: utf-8 -*-
# Generated by Django 1.9.1 on 2016-04-18 08:04
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('main_site', '0003_auto_20160331_1751'),
    ]

    operations = [
        migrations.AlterField(
            model_name='device',
            name='latitude',
            field=models.FloatField(),
        ),
        migrations.AlterField(
            model_name='device',
            name='longitude',
            field=models.FloatField(),
        ),
    ]
