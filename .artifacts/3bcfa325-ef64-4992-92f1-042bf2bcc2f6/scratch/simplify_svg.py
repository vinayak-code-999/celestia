import xml.etree.ElementTree as ET
import re

svg_path = r'C:/Users/vinay/Downloads/Gemini_Generated_Image_qcowaxqcowaxqcow.svg'
tree = ET.parse(svg_path)
root = tree.getroot()

path_strings = []
for elem in root.iter():
    if elem.tag.endswith('path'):
        d = elem.attrib.get('d', '')
        if d:
            path_strings.append(d)

combined_d = ' '.join(path_strings)

# Function to round numbers in path data to 2 decimal places to reduce string size
def round_numbers(match):
    num = float(match.group(0))
    return f"{num:.2f}".rstrip('0').rstrip('.')

# Replace all floats in path data with rounded versions
simplified_d = re.sub(r'-?\d+\.\d+', round_numbers, combined_d)

xml_content = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="2048"
    android:viewportHeight="2048">
    <path
        android:fillColor="#000000"
        android:pathData="{simplified_d}" />
</vector>
"""

target_path = r'C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/res/drawable/ic_launcher_foreground.xml'
open(target_path, 'w', encoding='utf-8').write(xml_content)
print(f"Successfully wrote simplified single-path XML, original size: {len(combined_d)}, new size: {len(simplified_d)}")
