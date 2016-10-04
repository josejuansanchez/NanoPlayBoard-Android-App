#!/bin/bash
#
# Subscribe
set -x

TOPIC='nanoplayboard'
BROKER_HOST='test.mosquitto.org'

mqtt sub -t $TOPIC -h $BROKER_HOST -v