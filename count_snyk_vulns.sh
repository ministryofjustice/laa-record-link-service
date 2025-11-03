#!/bin/bash

# ./count_vulns.sh file1.sarif file2.sarif ...

extract_severities() {
  local FILE=$1
  # Simplified jq: only trusts severity fields directly on the result object (.results[]?)
  # This prevents double-counting caused by the script finding severity both
  # on the result and through the rule map lookup.
  jq -r '
    .runs[0].results[]? |
    (
      # 1. High priority: Snyk-specific property (most reliable)
      .properties.snyk.severity?
      //
      # 2. Second priority: Standard SARIF "level" on the result object
      .level?
      //
      # 3. Final fallback
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

    # Count severities case-insensitively, handling common SARIF/Snyk levels
    while read -r severity; do
      # Lowercase severity for matching
      severity_lower=$(echo "$severity" | tr '[:upper:]' '[:lower:]')
      case "$severity_lower" in
        critical) ((CRITICAL++)) ;;
        high|error) ((HIGH++)) ;;     # Map 'error' to HIGH
        medium|warning) ((MEDIUM++)) ;; # Map 'warning' to MEDIUM
        low|note)      ((LOW++)) ;;
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