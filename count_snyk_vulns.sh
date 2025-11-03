#!/bin/bash
# count_snyk_vulns_fixed.sh
# Count Snyk vulnerabilities by severity from SARIF files

if [[ $# -eq 0 ]]; then
  echo "Usage: $0 <sarif_file1> [<sarif_file2> ...]"
  exit 1
fi

# Initialize counters
CRITICAL=0
HIGH=0
MEDIUM=0
LOW=0

extract_severities() {
  local FILE="$1"

  # Use jq to extract severity from results[], handle missing fields gracefully
  jq -r '
    .runs[]?.results[]? |
      (
        .level
        // .properties.problem.severity
        // .properties.severity
        // "unknown"
      )
  ' "$FILE" 2>/dev/null
}

for FILE in "$@"; do
  if [[ ! -f "$FILE" ]]; then
    echo "File not found: $FILE"
    continue
  fi

  echo "Processing $FILE"
  while read -r SEVERITY; do
    [[ -z "$SEVERITY" ]] && continue
    SEVERITY_LOWER=$(echo "$SEVERITY" | tr '[:upper:]' '[:lower:]')
    case "$SEVERITY_LOWER" in
      critical) ((CRITICAL++)) ;;
      high|error) ((HIGH++)) ;;
      medium|warning) ((MEDIUM++)) ;;
      low|note|info) ((LOW++)) ;;
      *) ;;  # ignore unknown
    esac
  done < <(extract_severities "$FILE")
done

echo "================================="
echo "CRITICAL=$CRITICAL"
echo "HIGH=$HIGH"
echo "MEDIUM=$MEDIUM"
echo "LOW=$LOW"
echo "================================="
