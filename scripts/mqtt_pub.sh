#!/bin/bash
#
# Publish
set -x

TOPIC='nanoplayboard'
MESSAGE='test message'
BROKER_HOST='test.mosquitto.org'

mqtt pub -t $TOPIC -h $BROKER_HOST -m "$MESSAGE"