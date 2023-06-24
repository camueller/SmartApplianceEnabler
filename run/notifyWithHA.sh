#! /usr/bin/env bash
# vim: set ft=bash

set -o errexit
set -o nounset
set -o pipefail
if [[ "${TRACE-0}" == "1" ]]; then set -o xtrace; fi

if [[ "${1-}" =~ ^-*h(elp)?$ ]]; then
    echo 'Usage: ./$0 sender_id device_name device_type device_vendor device_serial notification_key notification_text

Send a notifacation to registered Mobile Apps using REST API of Home Assistant.

'
    exit
fi

cd "$(dirname "$0")"

# User Long-Lived Access Tokens -- cf. https://developers.home-assistant.io/docs/auth_api/#making-authenticated-requests
TOKEN="XXX"
# Base URL of your home assistant
BASE_URL="https://homeassistant:8123"
# Which Mobile App should be notified. Use "notify" to notify all available Mobile Apps. -- cf. https://www.home-assistant.io/integrations/mobile_app/
NOTIFY_TARGET="notify"

main() {
  local sender_id="$1"
  # Device name from Device2EM.xml
  local device_name="$2"
  # Device type from Device2EM.xml
  local device_type="$3"
  # Device vendor from Device2EM.xml
  local device_vendor="$4"
  # Serial number of devive from Device2EM.xml
  local device_serial="$5"
  # Notification key of the event, e.g., CONTROL_OFF
  local notification_key="$6"
  # Notification text of event, e.g., "The device has been turned on."
  local notification_text="$7"

  local url="${BASE_URL}/api/services/notify/${NOTIFY_TARGET}"
  local title="SAE: ${device_name}"
  local msg="${notification_text}"
  local payload="{\"message\": \"${msg}\", \"title\": \"${title}\"}"

  curl --silent --insecure -H "Authorization: Bearer ${TOKEN}" -H "Content-Type: application/json" "${url}" -d "${payload}"
}

main "$@"

