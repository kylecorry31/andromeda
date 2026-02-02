import re
import os
import subprocess

gradle = open('build.gradle.kts', 'r')
contents = gradle.read()
gradle.close()

version_name = re.search("versionName = \"(.+)\"", contents).group(1)

# Get the list of new commits since the last tag
script_dir = os.path.dirname(os.path.abspath(__file__))
commits_result = subprocess.run(
    ['./scripts/list-new-commits.sh'], 
    capture_output=True, 
    text=True,
    cwd=script_dir
)
release_notes = commits_result.stdout.strip()

print("Creating draft release for version", version_name)

subprocess.run([
    'gh', 'release', 'create', version_name,
    '-t', version_name,
    '-n', release_notes,
    '-d'
])