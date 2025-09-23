#!/bin/bash
set -e

# Ensure /home/keyKeeper exists and is writable by appuser
mkdir -p /home/keyKeeper
chown -R appuser:appuser /home/keyKeeper

# Run the bot as appuser
exec su-exec appuser java -Duser.home=/home/ -jar /home/keykeeper.jar
