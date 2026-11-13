import os
from PIL import Image

src_png = r'C:/Users/vinay/Downloads/Gemini_Generated_Image_qcowaxqcowaxqcow.png'
img = Image.open(src_png).convert("RGBA")

res_dir = r'C:/Users/vinay/AndroidStudioProjects/celestia2/app/src/main/res'

# Mipmap densities and sizes
mipmap_sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

for folder, size in mipmap_sizes.items():
    folder_path = os.path.join(res_dir, folder)
    os.makedirs(folder_path, exist_ok=True)

    resized = img.resize((size, size), Image.Resampling.LANCZOS)

    # Save ic_launcher.webp
    launcher_path = os.path.join(folder_path, 'ic_launcher.webp')
    resized.save(launcher_path, 'WEBP', quality=90)

    # Save ic_launcher_round.webp
    round_path = os.path.join(folder_path, 'ic_launcher_round.webp')
    resized.save(round_path, 'WEBP', quality=90)
    print(f"Processed {folder}: {size}x{size}")

# Save mascot avatar drawable (e.g. 512x512)
drawable_dir = os.path.join(res_dir, 'drawable')
os.makedirs(drawable_dir, exist_ok=True)
avatar_resized = img.resize((512, 512), Image.Resampling.LANCZOS)
avatar_path = os.path.join(drawable_dir, 'mascot_avatar.webp')
avatar_resized.save(avatar_path, 'WEBP', quality=95)
print(f"Saved mascot avatar to {avatar_path}")
