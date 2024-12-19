import re
import os

gradle = open('build.gradle.kts', 'r')
contents = gradle.read()
gradle.close()

version_name = re.search("\"versionName\" = \"(.+)\"", contents).group(1)

print("Creating draft release for version", version_name)

os.system("gh release create " + version_name + " -d -t " + version_name)