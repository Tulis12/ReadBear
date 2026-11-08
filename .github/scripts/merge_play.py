import shutil

main = "fastlane/metadata"
add = "fastlane/play"
output = "fastlane/play_generated"

shutil.rmtree(output, ignore_errors=True)

shutil.copytree(main, output, dirs_exist_ok=True)
shutil.copytree(add, output, dirs_exist_ok=True)