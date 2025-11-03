#!/bin/bash

# Usage: ./count_snyk_vulns_fixed.sh file1.sarif [file2.sarif ...]
#
# This script parses SARIF files from Snyk and counts the number of vulnerabilities
# by severity: CRITICAL, HIGH, MEDIUM, LOW.
#
# It:
#  - Handles differences in how Snyk stores severities in SARIF depending on scan type
#  - Deduplicates repeated results (same ruleId + file location)
#  - Exports results for GitHub Actions

extract_severities() {
  local FILE=$1
  jq -r '
    # Build a map of ruleId → severity from the rules section
    ( .runs[].tool.driver.rules[]? |
      { (.id): (
          .properties.problem.severity
          // .properties.severity
          // .defaultConfiguration.level
          // "unknown"
        )
      }
    ) as $severityMap
    |
    # Collect results across all runs, include a simplified unique key to deduplicate
    [ .runs[].results[]? |
      {
        key: (
          (.ruleId // "") + ":" +
          (.locations[0].physicalLocation.artifactLocation.uri // "")
        ),
        severity: (
          .properties.problem.severity
          // .properties.severity
          // .level
          // $severityMap[.ruleId]
          // "unknown"
        )
      }
    ]
    | unique_by(.key)
    | .[].severity
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

      # Map SARIF levels to Snyk severities
      case "$severity_lower" in
        critical) ((CRITICAL++)) ;;
        high|error) ((HIGH++)) ;;
        medium|warning) ((MEDIUM++)) ;;
        low|note|info) ((LOW++)) ;;
        *) ;; # ignore unknown severities
      esac
    done <<< "$severities"
  else
    echo "File not found: $file"
  fi
done

echo "================================="
echo "CRITICAL=$CRITICAL"
echo "HIGH=$HIGH"
echo "MEDIUM=$MEDIUM"
echo "LOW=$LOW"
echo "================================="

# Export counts to GitHub Actions environment if running inside a workflow
if [[ -n "${GITHUB_ENV:-}" ]]; then
  {
    echo "CRITICAL=$CRITICAL"
    echo "HIGH=$HIGH"
    echo "MEDIUM=$MEDIUM"
    echo "LOW=$LOW"
  } >> "$GITHUB_ENV"
fi