import os

path_data = open(r'C:/Users/vinay/AndroidStudioProjects/celestia2/.artifacts/path.txt').read()
wrapped_path = '\n        '.join(path_data[i:i+80] for i in range(0, len(path_data), 80))

xml_content = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="2048"
    android:viewportHeight="2048">
    <path
        android:fillColor="#000000"
        android:pathData="{wrapped_path}" />
</vector>
"""

target_path = r'C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/res/drawable/ic_launcher_foreground.xml'
open(target_path, 'w', encoding='utf-8').write(xml_content)
print(f"Successfully wrote target foreground XML to {target_path}, size: {len(xml_content)}")
