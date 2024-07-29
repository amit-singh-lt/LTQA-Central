#!/bin/bash
# shellcheck disable=SC2059

# Declare global variables
fetch_video_data_response_body=""        # Holds the response body from API calls
response_log=""    # Holds log messages
video_duration=""  # Duration of the video fetched from ffprobe
test_duration=""   # Duration of the test fetched from the API
test_id=""         # Test ID extracted from the API responses


log_message() {
    local message="$1"
    response_log+="$message\n"
    echo -e "$message"
}


fetch_video_data() {
    log_message "Fetching video data from URL: $VIDEO_URL"
    local attempt=0
    local fetch_video_data_response_body
    local http_code
    local response_body

    while (( attempt < MAX_RETRIES )); do
        fetch_video_data_response_body=$(curl -s -w "%{http_code}" -o /dev/stdout "$VIDEO_URL")
        http_code="${fetch_video_data_response_body: -3}"
        response_body="${fetch_video_data_response_body:0:${#fetch_video_data_response_body}-3}"
        log_message "Response Code: $http_code"

        if [[ "$http_code" -eq 200 || "$http_code" -eq 304 ]]; then
            fetch_video_data_response_body="$response_body"
            return 0
        else
            log_message "Retrying in $RETRY_DELAY seconds..."
            sleep "$RETRY_DELAY"
        fi
        ((attempt++))
    done

    log_message "Failed to fetch video data after $((MAX_RETRIES * RETRY_DELAY)) seconds"
    exit 1
}


check_video_playable() {
    local video_url="$1"

    log_message "Checking if video is playable at URL: $video_url"

    local attempt=0
    local response_code

    while [ $attempt -lt "$MAX_RETRIES" ]; do
        # Fetch HTTP status code for video URL
        response_code=$(curl -s -o /dev/null -w "%{http_code}" "$video_url")
        log_message "Video Public Response Code: $response_code"

        if [ "$response_code" -eq 200 ] || [ "$response_code" -eq 304 ]; then
            log_message "Video is playable."
            return 0
        else
            log_message "Retrying in $RETRY_DELAY seconds..."
            sleep "$RETRY_DELAY"
        fi

        attempt=$((attempt + 1))
    done

    log_message "Video is not playable after $((MAX_RETRIES * RETRY_DELAY)) seconds"
    return 1
}

# Function to get video duration using ffprobe
get_video_duration() {
    local video_url="$1"

    log_message "Fetching video duration from URL: $video_url"
    # Fetch video duration using ffprobe and remove carriage returns
    video_duration=$(ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$video_url" | tr -d '\r')
    log_message "Video Duration is: $video_duration seconds"
}

# Function to get test duration from the API
get_test_duration() {
    local session_id="$1"
    local user_name="$2"
    local access_key="$3"

    session_api=$(printf "$SESSION_URL" "$user_name" "$access_key" "$session_id")
    log_message "Session API URL: $session_api"

    session_api_response=$(curl -s "$session_api")
    log_message "Session API Response: $session_api_response"

    test_id=$(echo "$session_api_response" | jq -r '.data.test_id')
    test_duration=$(echo "$session_api_response" | jq -r '.data.duration')
    log_message "Test ID: $test_id"
    log_message "Test Duration: $test_duration seconds"
}

# Function to check the duration of the video and the test
check_video_duration() {
    local session_id="$1"
    local user_name="$2"
    local access_key="$3"
    local video_url="$4"

    log_message "Checking video duration for session ID: $session_id"
    # Get the video duration and the test duration
    get_video_duration "$video_url"
    get_test_duration "$session_id" "$user_name" "$access_key"

    local duration_diff
    duration_diff=$(echo "$video_duration - $test_duration" | bc)

    # Check if the difference exceeds the permissible limit of 5 seconds
    local permissible_diff=5
    if (( $(echo "$duration_diff > $permissible_diff" | bc -l) )); then
        log_message "Duration mismatch: Video Duration = $video_duration seconds, Test Duration = $test_duration seconds, Difference = $duration_diff seconds. Exceeds permissible limit of $permissible_diff seconds."
        exit 1
    else
        log_message "Duration check passed: Video Duration = $video_duration seconds, Test Duration = $test_duration seconds, Difference = $duration_diff seconds."
    fi
}

check_end_list() {
    index_m3u8_url=$(printf "$INDEX_M3U8_URL" "$org_id" "$DATE" "$test_id")
    log_message "Fetching index.m3u8 from URL: $index_m3u8_url"
    response=$(curl -s "$index_m3u8_url")
    log_message "Index.m3u8 Response: $response"
    echo "$response" | grep -q "#EXT-X-ENDLIST"
}

# Function to check if the index.m3u8 file is correctly formatted
check_index_m3u8_for_sync() {
    local user_name="$1"
    local access_key="$2"

    test_api_url=$(printf "$TEST_URL" "$user_name" "$access_key" "$test_id")
    log_message "Test API URL: $test_api_url"

    org_id=$(curl -s "$test_api_url" | jq -r '.org_id')
    log_message "Organization ID: $org_id"


    log_message "Date: $DATE"

    local attempt=0
    while [ $attempt -lt "$MAX_RETRIES" ]; do
        if check_end_list; then
            log_message "#EXT-X-ENDLIST found"
            exit 0
        else
            log_message "Retrying in $RETRY_DELAY seconds..."
            sleep "$RETRY_DELAY"
        fi

        attempt=$((attempt + 1))
    done

    log_message "Fetching Video index.m3u8 data from URL: $index_m3u8_url"

    index_m3u8_url_response=$(curl -s "$index_m3u8_url")
    log_message "IndexM3U8 Response :- $index_m3u8_url_response"

    # Define an array of required lines to check
    required_lines_in_index_m3u8=(
        "#EXTM3U"
        "#START_TIMESTAMP"
        "#EXT-X-START:TIME-OFFSET"
        "#EXT-X-VERSION"
        "#EXT-X-TARGETDURATION"
        "#EXT-X-MEDIA-SEQUENCE"
        "#EXTINF"
        "#EXT-X-ENDLIST"
    )

    # Initialize a flag to track if any lines are missing
    missing_found=0

    # Check each required line
    for line in "${required_lines_in_index_m3u8[@]}"; do
        if echo "$index_m3u8_url_response" | grep -qF "$line"; then
            log_message "Found: $line"
        else
            log_message "Missing: $line"
            missing_found=1
        fi
    done

    # Check if index0.ts is present
    if echo "$index_m3u8_url_response" | grep -q "index0.ts"; then
        log_message "index0.ts is present"
    else
        log_message "index0.ts is missing"
        missing_found=1
    fi

    # Exit with a non-zero status if any lines were missing
    if [ $missing_found -eq 1 ]; then
        exit 1
    fi

    log_message "All checks passed successfully."
}

# Function to verify the test video
verify_test_video() {
    local video_url_from_response
    video_url_from_response=$(echo "$fetch_video_data_response_body" | jq -r '.url')
    log_message "Extracted Video URL: $video_url_from_response"
    if [ -z "$video_url_from_response" ]; then
        log_message "Video URL is not present in the response."
        exit 1
    fi
    if check_video_playable "$video_url_from_response"; then
        log_message "Video is generated and playable."
    else
        log_message "Video is not playable after several retries."
        exit 1
    fi
}

# Ensure that all required parameters are provided
if [ $# -lt 6 ]; then
    log_message "Usage: $0 <USER_NAME> <ACCESS_KEY> <HUB> <SESSION_ID> <S3_BUCKET> <ORG_ID> <RETRY_DELAY> <MAX_RETRIES>"
    exit 1
fi

# Configuration
# Retrieve arguments
USER_NAME="$1"
ACCESS_KEY="$2"
HUB="$3"
SESSION_ID="$4"
S3_BUCKET="$5"
ORG_ID="$6"
RETRY_DELAY="${7:-10}"
MAX_RETRIES="${8:-3}"
DATE=$(date -u +"%Y/%m/%d")
VIDEO_URL="https://$USER_NAME:$ACCESS_KEY@$HUB/automation/api/v1/sessions/$SESSION_ID/video"
SESSION_URL="https://$USER_NAME:$ACCESS_KEY@$HUB/automation/api/v1/sessions/$SESSION_ID"
TEST_URL="https://$USER_NAME:$ACCESS_KEY@$HUB/api/v1/test/$SESSION_ID"
INDEX_M3U8_URL="https://$S3_BUCKET/orgId-$ORG_ID/$DATE/$SESSION_ID/video/index.m3u8"

# Function: fetch_video_data
# Description: This function attempts to fetch video data from a specified URL.
# It uses curl to make a GET request and retries if the request fails, up to a
# maximum number of retries specified by the MAX_RETRIES variable. If a successful
# HTTP response (200 or 304) is received, it captures the response body. If the
# maximum retries are reached without a successful response, the function logs
# an error message and exits the script with a non-zero status code.
fetch_video_data "$session_id" "$user_name" "$access_key"

# Call the main function to verify the video
verify_test_video

check_video_duration "$session_id" "$user_name" "$access_key" "$video_url_from_response"
check_index_m3u8_for_sync "$user_name" "$access_key"


# Output the collected log messages
echo -e "$response_log"
