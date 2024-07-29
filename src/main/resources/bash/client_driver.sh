#!/bin/bash
# shellcheck disable=SC2059
CLIENT_DRIVER_CAPABILITIES_URL="https://%s:%s@%s/wd/hub/session"
CLIENT_DRIVER_CAPABILITIES='{
  "capabilities": {
    "lt:options": {
      "browserName": "Chrome",
      "build": "Build",
      "commandLog": true,
      "maxDuration": 3,
      "name": "Test Name",
      "network": "true",
      "platform": "Windows 10",
      "queueTimeout": "300",
      "version": "latest",
      "visual": true
    }
  }
}'

DELETE_SESSION_URL="$CLIENT_DRIVER_CAPABILITIES_URL/%s"

IMPLICIT_WAIT_TIMEOUT_URL="$CLIENT_DRIVER_CAPABILITIES_URL/%s/timeouts"
IMPLICIT_WAIT_TIMEOUT_BODY='{
 "implicit": %s
}'


GET_URL="$CLIENT_DRIVER_CAPABILITIES_URL/%s/url"
GET_URL_BODY='{ "url": "%s" }'

DOCUMENT_COOKIES_URL="$CLIENT_DRIVER_CAPABILITIES_URL/%s/execute/sync"
DOCUMENT_COOKIES_BODY="{ \"args\": [ ], \"script\": \"document.cookie = 'accessToken=%s; Path=/; domain=%s'\" }"

# Configuration
SHORTEST_WAIT=1    # seconds

# Declare global variables
response_log=""    # Holds log messages
access_token=""


# Function to append messages to the global response_log and print them
log_message() {
    local message="$1"
    response_log+="$message\n"
    echo "$message"
}

create_driver() {
    client_driver_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$CLIENT_DRIVER_CAPABILITIES" "$CLIENT_DRIVER_CAPABILITIES_URL")
    log_message "Client Driver Response :- $client_driver_response"

    session_id=$(echo "$client_driver_response" | jq -r '.value.sessionId')
    log_message "Session ID :- $session_id"
}

implicit_wait_10() {
    implicit_wait_timeout_url=$(printf "$IMPLICIT_WAIT_TIMEOUT_URL" "$session_id")
    log_message "Implicit Wait Timeout URL: $implicit_wait_timeout_url"

    implicit_wait_timeout_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$IMPLICIT_WAIT_TIMEOUT_BODY" "$implicit_wait_timeout_url")
    log_message "Implicit Wait Timeout Response :- $implicit_wait_timeout_response"
}

access_token() {
    access_token=$(curl -s -D - -X POST -H "Content-Type: application/json" -d '{ "email": "%s", "password": "%s" }' https://auth.lambdatest.com/api/login | grep -i 'Set-Cookie' | grep -o 'accessToken=[^;]*' | cut -d '=' -f2)
    log_message "Access Token for Login :- $access_token"
}

get_url() {
    local url="$1"

    get_url=$(printf "$GET_URL" "$session_id")
    log_message "Get URL: $get_url"

    get_url_body=$(printf "$GET_URL_BODY" "$url")
    log_message "Get URL Body: $get_url_body"

    get_url_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$get_url_body" "$get_url")
    log_message "Get URL Response :- $get_url_response"

    sleep $SHORTEST_WAIT
}

set_access_token_cookie() {
    document_cookie_url=$(printf "$DOCUMENT_COOKIES_URL" "$session_id")
    log_message "Document Cookie URL :- $document_cookie_url"

    document_cookie_body=$(printf "$DOCUMENT_COOKIES_BODY" "$access_token")
    log_message "Document Cookie Body :- $document_cookie_body"

    document_cookie_response=$(curl -s -X POST -H "Content-Type: application/json" -d "$document_cookie_body" "$document_cookie_url")
    log_message "Document Cookie Response :- $document_cookie_response"
}

delete_session() {
    delete_session_url=$(printf "$DELETE_SESSION_URL" "$session_id")
    log_message "Delete Session URL: $delete_session_url"

    delete_session_response=$(curl -s -X DELETE "$delete_session_url")
    log_message "Delete Session Response :- $delete_session_response"
}

create_driver
implicit_wait_10
access_token
get_url ""
set_access_token_cookie
get_url ""
delete_session
