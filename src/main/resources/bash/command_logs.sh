#!/bin/bash
# shellcheck disable=SC2059

# Configuration
COMMAND_LOGS_API="https://%s:%s@%s/automation/api/v1/sessions/%s/log/command"

# Declare global variables
command_log_response=""        # Holds the response body from API calls
response_log=""    # Holds log messages
MAX_RETRIES=1
RETRY_DELAY=10  # seconds

# Function to append messages to the global response_log and print them
log_message() {
    local message="$1"
    response_log+="$message\n"
    echo "$message"
}

fetch_command_logs() {
    local session_id="$1"
    local user_name="$2"
    local access_key="$3"
    local command_url

    # Format the video URL using provided user credentials and session ID
    command_url=$(printf "$COMMAND_LOGS_API" "$user_name" "$access_key" "$session_id")
    log_message "Command Log API URL: $command_url"

    local attempt=0
    while [ $attempt -lt $MAX_RETRIES ]; do
        # Fetch response and status code using process substitution
        command_log_response=$(curl -s "$command_url")

        command_log_response_total_count=$(echo "$command_log_response" | jq -r '.total')
        echo "Command Log Total Count :- $command_log_response_total_count"

        command_log_response_status=$(echo "$command_log_response" | jq -r '.status')
        echo "Command Log Response Status :- $command_log_response_status"

        command_log_response_message=$(echo "$command_log_response" | jq -r '.message')
        echo "Command Log Response Message :- $command_log_response_message"


        if [ "$command_log_response_total_count" -ne 0 ] || [ "$command_log_response_status" == "success" ] || [ "$command_log_response_message" == "Retrieve logs list was successful" ]; then
            log_message "Command Log fetched successfully."
            return 0
        else
            log_message "Retrying in $RETRY_DELAY seconds..."
            sleep $RETRY_DELAY
        fi

        attempt=$((attempt + 1))
    done

    log_message "Failed to fetch command log data after $((MAX_RETRIES * RETRY_DELAY)) seconds"
    log_message "Command Log API Response :- $command_log_response"
    exit 1
}

fetch_command_logs_request_and_response() {
    # command_log_response_message=$(echo "$command_log_response" | jq -r '.data[] | {requestPath: .Value.requestPath, requestBody: .Value.requestBody, responseBody: .Value.responseBody, responseStatus: .Value.responseStatus}')
    command_log_response_message=$(echo "$command_log_response" | jq -r '.data[] | "\(.Value.requestPath) \(.Value.requestBody) \(.Value.responseBody) \(.Value.responseStatus)"')
}

# Function to verify the test video
verify_command_log() {
    local session_id="$1"
    local user_name="$2"
    local access_key="$3"

    # Fetch video data from API
    fetch_command_logs "$session_id" "$user_name" "$access_key"

    fetch_command_logs_request_and_response
}

# Ensure that all required parameters are provided
if [ $# -ne 3 ]; then
    log_message "Usage: $0 <session_id> <user_name> <access_key>"
    exit 1
fi

# Retrieve arguments
session_id="$1"
user_name="$2"
access_key="$3"

# Call the main function to verify the video
verify_command_log "$session_id" "$user_name" "$access_key"

# Output the collected log messages
echo -e "$response_log"
