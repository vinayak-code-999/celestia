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

# Simplified approach: Since SVG paths from AI generators are often composed of M / m commands,
# let's parse commands and their coordinates, converting relative lowercase commands to absolute uppercase commands.

def tokenize_path(path_str):
    # Regex to match commands and numbers
    pattern = r'([a-zA-Z])|(-?\d*\.?\d+(?:e-?\d+)?)'
    tokens = []
    for match in re.finditer(pattern, path_str):
        cmd, num = match.groups()
        if cmd:
            tokens.append(cmd)
        elif num:
            tokens.append(float(num))
    return tokens

tokens = tokenize_path(combined_d)

# Convert relative commands to absolute and group into sub-paths starting with M
sub_paths = []
current_subpath = []
curr_x, curr_y = 0.0, 0.0
start_x, start_y = 0.0, 0.0
i = 0
current_cmd = 'M'

while i < len(tokens):
    token = tokens[i]
    if isinstance(token, str):
        current_cmd = token
        i += 1
        if i >= len(tokens):
            break
        token = tokens[i]

    is_relative = current_cmd.islower()
    abs_cmd = current_cmd.upper()

    # Determine parameter count for command
    param_counts = {
        'M': 2, 'L': 2, 'H': 1, 'V': 1,
        'C': 6, 'S': 4, 'Q': 4, 'T': 2,
        'A': 7, 'Z': 0
    }

    base_cmd = abs_cmd
    if base_cmd not in param_counts:
        i += 1
        continue

    n_params = param_counts[base_cmd]

    if base_cmd == 'Z':
        current_subpath.append('Z')
        curr_x, curr_y = start_x, start_y
        if current_subpath:
            sub_paths.append(''.join(current_subpath))
            current_subpath = []
        i += 1
        continue

    while i < len(tokens) and not isinstance(tokens[i], str):
        coords = []
        for _ in range(n_params):
            if i < len(tokens) and not isinstance(tokens[i], str):
                coords.append(tokens[i])
                i += 1
            else:
                break
        if len(coords) < n_params:
            break

        # Process coordinates
        if base_cmd == 'M':
            if is_relative:
                curr_x += coords[0]
                curr_y += coords[1]
            else:
                curr_x, curr_y = coords[0], coords[1]
            start_x, start_y = curr_x, curr_y
            if current_subpath:
                sub_paths.append(''.join(current_subpath))
                current_subpath = []
            current_subpath.append(f"M {curr_x:.2f},{curr_y:.2f}")
            # Subsequent coordinate pairs after M are treated as L
            base_cmd = 'L'

        elif base_cmd == 'L':
            if is_relative:
                curr_x += coords[0]
                curr_y += coords[1]
            else:
                curr_x, curr_y = coords[0], coords[1]
            current_subpath.append(f" L {curr_x:.2f},{curr_y:.2f}")

        elif base_cmd == 'H':
            val = coords[0]
            if is_relative:
                curr_x += val
            else:
                curr_x = val
            current_subpath.append(f" H {curr_x:.2f}")

        elif base_cmd == 'V':
            val = coords[0]
            if is_relative:
                curr_y += val
            else:
                curr_y = val
            current_subpath.append(f" V {curr_y:.2f}")

        elif base_cmd == 'C':
            if is_relative:
                x1, y1 = curr_x + coords[0], curr_y + coords[1]
                x2, y2 = curr_x + coords[2], curr_y + coords[3]
                curr_x, curr_y = curr_x + coords[4], curr_y + coords[5]
            else:
                x1, y1 = coords[0], coords[1]
                x2, y2 = coords[2], coords[3]
                curr_x, curr_y = coords[4], coords[5]
            current_subpath.append(f" C {x1:.2f},{y1:.2f} {x2:.2f},{y2:.2f} {curr_x:.2f},{curr_y:.2f}")

        elif base_cmd == 'S':
            if is_relative:
                x2, y2 = curr_x + coords[0], curr_y + coords[1]
                curr_x, curr_y = curr_x + coords[2], curr_y + coords[3]
            else:
                x2, y2 = coords[0], coords[1]
                curr_x, curr_y = coords[2], coords[3]
            current_subpath.append(f" S {x2:.2f},{y2:.2f} {curr_x:.2f},{curr_y:.2f}")

        elif base_cmd == 'Q':
            if is_relative:
                x1, y1 = curr_x + coords[0], curr_y + coords[1]
                curr_x, curr_y = curr_x + coords[2], curr_y + coords[3]
            else:
                x1, y1 = coords[0], coords[1]
                curr_x, curr_y = coords[2], coords[3]
            current_subpath.append(f" Q {x1:.2f},{y1:.2f} {curr_x:.2f},{curr_y:.2f}")

        elif base_cmd == 'T':
            if is_relative:
                curr_x += coords[0]
                curr_y += coords[1]
            else:
                curr_x, curr_y = coords[0], coords[1]
            current_subpath.append(f" T {curr_x:.2f},{curr_y:.2f}")

        elif base_cmd == 'A':
            rx, ry, rot, large_arc, sweep = coords[0], coords[1], coords[2], int(coords[3]), int(coords[4])
            if is_relative:
                curr_x += coords[5]
                curr_y += coords[6]
            else:
                curr_x, curr_y = coords[5], coords[6]
            current_subpath.append(f" A {rx:.2f},{ry:.2f} {rot:.2f} {large_arc},{sweep} {curr_x:.2f},{curr_y:.2f}")

if current_subpath:
    sub_paths.append(''.join(current_subpath))

print(f"Total extracted absolute sub-paths: {len(sub_paths)}")

# Group sub-paths into batches so each <path> element is small (< 15KB)
batches = []
current_batch = []
current_batch_len = 0

for sp in sub_paths:
    if current_batch_len + len(sp) > 15000:
        batches.append(' '.join(current_batch))
        current_batch = [sp]
        current_batch_len = len(sp)
    else:
        current_batch.append(sp)
        current_batch_len += len(sp)
if current_batch:
    batches.append(' '.join(current_batch))

print(f"Grouped into {len(batches)} <path> elements")

path_elements = []
for b in batches:
    path_elements.append(f'    <path\n        android:fillColor="#000000"\n        android:pathData="{b}" />')

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
print(f"Successfully wrote absolute-converted foreground XML to {target_path}")
