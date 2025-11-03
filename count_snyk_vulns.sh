#!/bin/bash

# ./count_vulns.sh file1.sarif file2.sarif ...

extract_severities() {
  local FILE=$1
  jq -r '
    # 1. Build a map from ruleId to severity (from rule definition) - useful for SnykCode
    (
      .runs[0].tool.driver.rules[]? |
      { (.id): (
          .properties.problem.severity // .defaultConfiguration.level // "unknown"
        )
      }
    ) as $severityMap
    |
    # 2. Iterate through results and try to get severity from the result itself first,
    #    then fall back to the rule map
    .runs[0].results[]? |
    (
      # Check Snyk-specific property first
      .properties.snyk.severity?
      //
      # Check SARIF "level" property
      .level?
      //
      # Fallback to the severity map created in step 1
      $severityMap[.ruleId]?
      //
      # Final fallback if none of the above are present
      "unknown"
    )
  ' "$FILE"
}

if [[ $# -eq 0 ]]; then
  echo "Usage: $0 file1.sarif [file2.sarif ...]"
  exit 1
fi

CRITICAL=0
HIGH=0
MEDIUM=0
LOW=0

for file in "$@"; do
  if [[ -f "$file" ]]; then
    echo "Processing $file"
    # Safely call the function and check if output is empty
    severities=$(extract_severities "$file")
    if [[ -z "$severities" ]]; then
      echo "  No results or severity data extracted from $file."
      continue
    fi

    # Count severities case-insensitively without ${var,,} for bash <4
    while read -r severity; do
      # Lowercase severity for matching
      severity_lower=$(echo "$severity" | tr '[:upper:]' '[:lower:]')
      case "$severity_lower" in
        critical) ((CRITICAL++)) ;;
        high)     ((HIGH++)) ;;
        medium)   ((MEDIUM++)) ;;
        low)      ((LOW++)) ;;
      esac
    done <<< "$severities"
  else
    echo "File not found: $file"
  fi
done

# Output results
echo "CRITICAL=$CRITICAL"
echo "HIGH=$HIGH"
echo "MEDIUM=$MEDIUM"
echo "LOW=$LOW"

# Export to GitHub Actions environment file if applicable
if [[ -n "$GITHUB_ENV" ]]; then
  {
    echo "CRITICAL=$CRITICAL"
    echo "HIGH=$HIGH"
    echo "MEDIUM=$MEDIUM"
    echo "LOW=$LOW"
  } >> "$GITHUB_ENV"
fi