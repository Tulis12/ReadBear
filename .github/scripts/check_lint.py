import xml.etree.ElementTree as ET
import sys

REPORT = "app/build/reports/lint-results-debug.xml"

# This checks will cause the actions to fail
ALLOWED = {
    "PluralsCandidate",
    "IconLocation"
}

root = ET.parse(REPORT).getroot()

errors = []

for issue in root.findall("issue"):
    issue_id = issue.get("id")

    if issue_id in ALLOWED:
        errors.append(issue)

if errors:
    print(f"Found {len(errors)} selected lint issues:\n")

    for issue in errors:
        print(
            f"{issue.get('id')}: "
            f"{issue.get('message')}"
        )

        location = issue.find("location")
        if location is not None:
            print(f"  {location.get('file')}:{location.get('line')}")

    sys.exit(1)

print("No selected lint issues found.")