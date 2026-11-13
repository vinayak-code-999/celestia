import os
from PIL import Image

downloads = r'C:/Users/vinay/Downloads'
for f in os.listdir(downloads):
    if f.lower().endswith('.png'):
        path = os.path.join(downloads, f)
        try:
            im = Image.open(path)
            w, h = im.size
            if w > h * 1.5:  # Grid image is wider than tall (4x2)
                print(f"Found grid image: {f} ({w}x{h})")
        except Exception as e:
            pass
