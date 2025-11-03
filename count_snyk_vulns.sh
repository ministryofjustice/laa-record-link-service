#!/bin/bash

# count_snyk_vulns_fixed.sh
# Usage: ./count_snyk_vulns_fixed.sh file1.sarif [file2.sarif ...]

if [[ $# -eq 0 ]]; then
  echo "Usage: $0 <sarif_file1> [<sarif_file2> ...]"
  exit 1
fi

# Extract severities from a SARIF file, handling empty results gracefully
extract_severities() {
  local FILE=$1
  jq -r '
    # Build ruleId → severity map with fallbacks
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
    # Collect results with a unique key: ruleId + first location URI or message
    [ .runs[].results[]? |
      {
        key: (.ruleId // "") + ":" +
             (.locations[0].physicalLocation.artifactLocation.uri // (.message.text // "")),
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

# Initialize counts
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
        high|error) ((HIGH++)) ;;
        medium|warning) ((MEDIUM++)) ;;
        low|note|info) ((LOW++)) ;;
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

# Export to GitHub Actions environment if applicable
if [[ -n "$GITHUB_ENV" ]]; then
  {
    echo "CRITICAL=$CRITICAL"
    echo "HIGH=$HIGH"
    echo "MEDIUM=$MEDIUM"
    echo "LOW=$LOW"
  } >> "$GITHUB_ENV"
fi
