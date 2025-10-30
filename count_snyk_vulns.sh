#!/bin/bash

# Usage: ./count_snyk_vulns_fixed.sh file1.sarif [file2.sarif ...]

# This script parses SARIF files from Snyk and counts the number of vulnerabilities
# by severity: CRITICAL, HIGH, MEDIUM, LOW.
# It handles differences in how Snyk stores severities in SARIF depending on the scan type.

extract_severities() {
  local FILE=$1
  jq -r '
    # Build a map of ruleId → severity, using both properties and defaultConfiguration fields.
    (
      .runs[0].tool.driver.rules[]? |
      { (.id): (
          .properties.problem.severity
          // .properties.severity
          // .defaultConfiguration.level
          // "unknown"
        )
      }
    ) as $severityMap
    |
    # For each result, prefer severity in the result itself, else fallback to rule map
    .runs[0].results[]? |
    (
      .properties.problem.severity
      // .properties.severity
      // $severityMap[.ruleId]
      // "unknown"
    )
  ' "$FILE" 2>/dev/null || echo ""
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
    severities=$(extract_severities "$file")
    while read -r severity; do
      [[ -z "$severity" ]] && continue
      severity_lower=$(echo "$severity" | tr '[:upper:]' '[:lower:]')
      case "$severity_lower" in
        critical) ((CRITICAL++)) ;;
        high)     ((HIGH++)) ;;
        medium)   ((MEDIUM++)) ;;
        low)      ((LOW++)) ;;
        *)        ;; # ignore unknown or info
      esac
    done <<< "$severities"
  else
    echo "File not found: $file"
  fi
done

# Output summary
echo "CRITICAL=$CRITICAL"
echo "HIGH=$HIGH"
echo "MEDIUM=$MEDIUM"
echo "LOW=$LOW"

# Export counts to GitHub Actions env if running in workflow
if [[ -n "${GITHUB_ENV:-}" ]]; then
  {
    echo "CRITICAL=$CRITICAL"
    echo "HIGH=$HIGH"
    echo "MEDIUM=$MEDIUM"
    echo "LOW=$LOW"
  } >> "$GITHUB_ENV"
fi
