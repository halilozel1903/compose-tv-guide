#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running Android TV emulator (1080p).
# D-pad presses can't be timed reliably through adb on a fresh emulator, so the sample sets up each scene
# from the `scene` extra and logs SCENE_READY once focus is in place. All channels and programmes come from
# the sample's bundled demo guide (made up, labelled as demo data on screen).
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

install_sample

# Today, the first channel's programme on now focused.
fresh_launch --es scene guide
wait_for_scene guide
capture tv-guide "ON NOW"

# Today, a later programme on the fourth channel focused, its details in the hero area.
fresh_launch --es scene detail
wait_for_scene detail
capture tv-focused-detail "Demo Sports"

# Tomorrow picked in the day switcher, which has focus.
fresh_launch --es scene day
wait_for_scene day
capture tv-day-switch "UPCOMING"
