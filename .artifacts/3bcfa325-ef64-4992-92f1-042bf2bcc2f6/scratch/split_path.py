import xml.etree.ElementTree as ET
import re

svg_path = r'C:/Users/vinay/Downloads/Gemini_Generated_Image_qcowaxqcowaxqcow.svg'
tree = ET.parse(svg_path)
root = tree.getroot()

# Extract all path data elements from SVG
path_strings = []
for elem in root.iter():
    if elem.tag.endswith('path'):
        d = elem.attrib.get('d', '')
        if d:
            path_strings.append(d)

combined_d = ' '.join(path_strings)

# Split by 'm ' or 'M ' commands to create individual sub-paths
sub_paths = re.split(r'(?=[mM])', combined_d)

path_elements = []
for sp in sub_paths:
    sp = sp.strip()
    if sp:
        path_elements.append(f'    <path\n        android:fillColor="#000000"\n        android:pathData="{sp}" />')

xml_content = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="2048"
    android:viewportHeight="2048">
{'\n'.join(path_elements)}
</vector>
"""

target_path = r'C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/res/drawable/ic_launcher_foreground.xml'
open(target_path, 'w', encoding='utf-8').write(xml_content)
print(f"Successfully wrote split foreground XML with {len(path_elements)} paths to {target_path}")
