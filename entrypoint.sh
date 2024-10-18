#!/bin/sh
rc-update add docker boot
service docker start
exec "$@"
