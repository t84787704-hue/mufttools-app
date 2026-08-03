import os
import subprocess

res_dir = 'app/src/main/res'
densities = {
    'mdpi': (48, 48),
    'hdpi': (72, 72),
    'xhdpi': (96, 96),
    'xxhdpi': (144, 144),
    'xxxhdpi': (192, 192)
}

print('=== 1. Generating 1024x1024 Launcher Icon Master (No Text) ===')

master_icon_cmd = [
    'convert', '-size', '1024x1024', 'xc:none',
    
    # Deep Purple / Violet Gradient Base Squircle Canvas
    '(', '-size', '1024x1024', 'xc:#1E0836', '-draw', 'roundrectangle 24,24 1000,1000 220,220', ')', '-composite',
    '(', '-size', '1024x1024', 'xc:#4C1D95', '-draw', 'roundrectangle 24,24 1000,750 200,200', ')', '-composite',
    '(', '-size', '1024x1024', 'xc:#0F0326', '-draw', 'roundrectangle 24,620 1000,1000 200,200', ')', '-composite',

    # Outer Subtle Blue / Violet Glow Rim Edge
    '-stroke', '#8B5CF6', '-strokewidth', '8', '-fill', 'none',
    '-draw', 'roundrectangle 36,36 988,988 208,208',
    '-stroke', 'none',

    # Dark Cavity Interior of Lid
    '-fill', '#110328',
    '-draw', 'roundrectangle 190,200 834,500 56,56',

    # Silver Metallic Wrench Handle in Lid
    '-stroke', '#E2E8F0', '-strokewidth', '32', '-fill', 'none',
    '-draw', 'line 390,230 390,140',
    '-draw', 'line 390,140 634,140',
    '-draw', 'line 634,140 634,230',
    '-stroke', '#94A3B8', '-strokewidth', '16',
    '-draw', 'line 390,230 390,140',
    '-draw', 'line 390,140 634,140',
    '-draw', 'line 634,140 634,230',
    '-stroke', 'none',

    # 4 Tool Badges Inside Cavity (PDF, Image/Photo, QR Code, Video) - NO TEXT!
    # A. PDF Badge (Red Document) - Top-Left
    '(',
      '-size', '170x210', 'xc:none',
      '-fill', 'white', '-draw', 'roundrectangle 0,0 170,210 24,24',
      '-fill', '#EF4444', '-draw', 'roundrectangle 0,0 170,100 24,24',
      '-fill', '#EF4444', '-draw', 'rectangle 0,50 170,100',
      # Folded Page Edge & Document Lines (No Text)
      '-fill', '#B91C1C', '-draw', 'polygon 120,0 170,50 120,50',
      '-fill', '#CBD5E1', '-draw', 'roundrectangle 25,130 145,145 6,6',
      '-fill', '#CBD5E1', '-draw', 'roundrectangle 25,160 115,175 6,6',
      '-rotate', '-14',
    ')', '-geometry', '+140+235', '-composite',

    # B. Gallery Photo Badge (Blue Image) - Bottom-Left
    '(',
      '-size', '170x170', 'xc:none',
      '-fill', '#3B82F6', '-draw', 'roundrectangle 0,0 170,170 30,30',
      '-fill', '#FEF08A', '-draw', 'circle 125,45 125,60',
      '-fill', 'white', '-draw', 'polygon 15,150 65,85 100,125 125,95 155,150',
      '-rotate', '-6',
    ')', '-geometry', '+295+305', '-composite',

    # C. QR Code Badge (White/Slate QR Tile) - Top-Right
    '(',
      '-size', '170x170', 'xc:none',
      '-fill', 'white', '-draw', 'roundrectangle 0,0 170,170 30,30',
      '-fill', '#0F172A', '-draw', 'roundrectangle 20,20 68,68 10,10',
      '-fill', '#0F172A', '-draw', 'roundrectangle 102,20 150,68 10,10',
      '-fill', '#0F172A', '-draw', 'roundrectangle 20,102 68,150 10,10',
      '-fill', 'white', '-draw', 'roundrectangle 29,29 59,59 5,5',
      '-fill', 'white', '-draw', 'roundrectangle 111,29 141,59 5,5',
      '-fill', 'white', '-draw', 'roundrectangle 29,111 59,141 5,5',
      '-fill', '#0F172A', '-draw', 'roundrectangle 37,37 51,51 3,3',
      '-fill', '#0F172A', '-draw', 'roundrectangle 119,37 133,51 3,3',
      '-fill', '#0F172A', '-draw', 'roundrectangle 37,119 51,133 3,3',
      '-fill', '#0F172A', '-draw', 'rectangle 85,85 105,105',
      '-fill', '#0F172A', '-draw', 'rectangle 110,110 140,140',
      '-rotate', '7',
    ')', '-geometry', '+535+295', '-composite',

    # D. Video Player Badge (Purple Video) - Bottom-Right
    '(',
      '-size', '180x180', 'xc:none',
      '-fill', '#8B5CF6', '-draw', 'roundrectangle 0,0 180,180 34,34',
      '-stroke', 'white', '-strokewidth', '6', '-fill', 'none',
      '-draw', 'roundrectangle 16,16 164,164 22,22',
      '-stroke', 'none', '-fill', 'white',
      '-draw', 'polygon 72,55 125,90 72,125',
      '-rotate', '15',
    ')', '-geometry', '+680+250', '-composite',

    # Metallic Wrench Mechanism in Center
    '(',
      '-size', '200x380', 'xc:none',
      '-fill', '#F8FAFC', '-draw', 'circle 100,75 100,150',
      '-fill', '#110328', '-draw', 'polygon 78,0 122,0 100,75',
      '-fill', '#F8FAFC', '-draw', 'roundrectangle 62,110 138,340 14,14',
      '-fill', '#CBD5E1', '-draw', 'roundrectangle 20,280 180,350 18,18',
      '-fill', '#475569', '-draw', 'roundrectangle 45,305 155,325 8,8',
    ')', '-geometry', '+412+180', '-composite',

    # Main Metallic Purple Toolbox Front Body
    '(',
      '-size', '1024x1024', 'xc:none',
      '-fill', '#2E0854', '-draw', 'roundrectangle 110,470 914,930 54,54',
      '-fill', '#5B21B6', '-draw', 'roundrectangle 110,470 914,800 48,48',
      '-fill', '#7C3AED', '-draw', 'roundrectangle 94,445 930,525 40,40',
      '-stroke', '#DDD6FE', '-strokewidth', '4', '-fill', 'none',
      '-draw', 'roundrectangle 102,453 922,517 32,32',
    ')', '-composite',

    # Center Metallic Latch Plate
    '(',
      '-size', '140x220', 'xc:none',
      '-fill', '#F1F5F9', '-draw', 'roundrectangle 10,10 130,210 24,24',
      '-fill', '#475569', '-draw', 'roundrectangle 30,40 110,140 12,12',
      '-fill', '#0284C7', '-draw', 'circle 70,175 70,190',
    ')', '-geometry', '+442+520', '-composite',

    # Bottom Corner Guards
    '-fill', '#E2E8F0',
    '-draw', 'roundrectangle 110,870 200,930 20,20',
    '-draw', 'roundrectangle 824,870 914,930 20,20',

    'app/src/main/res/drawable/playstore_master_hd.png'
]

subprocess.run(master_icon_cmd, check=True)
print('Master Launcher Icon (1024x1024, No Text) rendered!')

print('=== 2. Generating Mipmap Density Launcher Icons (PNG & WebP) ===')
for density, (w, h) in densities.items():
    out_dir = os.path.join(res_dir, f'mipmap-{density}')
    os.makedirs(out_dir, exist_ok=True)
    
    # Square launcher
    ic_webp = os.path.join(out_dir, 'ic_launcher.webp')
    subprocess.run(['convert', 'app/src/main/res/drawable/playstore_master_hd.png', '-resize', f'{w}x{h}', '-quality', '95', ic_webp], check=True)
    
    # Round launcher
    ic_round_webp = os.path.join(out_dir, 'ic_launcher_round.webp')
    cmd_round_webp = [
        'convert', 'app/src/main/res/drawable/playstore_master_hd.png', '-resize', f'{w}x{h}',
        '(', '-size', f'{w}x{h}', 'xc:none', '-fill', 'white', '-draw', f'circle {w//2},{h//2} {w//2},0', ')',
        '-compose', 'DstIn', '-composite',
        '-quality', '95', ic_round_webp
    ]
    subprocess.run(cmd_round_webp, check=True)
    print(f'Generated {density} ({w}x{h}) WebP icons.')

print('=== 3. Generating Splash Screen HD Image (1080x1920) ===')
# First create 512x512 splash logo
splash_logo_cmd = [
    'convert', 'app/src/main/res/drawable/playstore_master_hd.png',
    '-resize', '512x512',
    'app/src/main/res/drawable/splash_logo.png'
]
subprocess.run(splash_logo_cmd, check=True)

splash_screen_cmd = [
    'convert', '-size', '1080x1920', 'xc:none',
    
    # Deep purple gradient background
    '(', '-size', '1080x1920', 'xc:#0F0326', ')', '-composite',
    '(', '-size', '1080x1920', 'xc:#3B0764', '-draw', 'circle 540,800 540,150', ')', '-composite',

    # Glow effect overlay
    '-stroke', '#8B5CF6', '-strokewidth', '2', '-fill', 'none',
    '-draw', 'circle 540,800 540,500',
    '-stroke', 'none',

    # Centered Logo (440x440)
    '(', 'app/src/main/res/drawable/splash_logo.png', '-resize', '440x440', ')',
    '-geometry', '+320+540', '-composite',

    # App Title "MuftTools"
    '-font', 'DejaVu-Sans-Bold', '-pointsize', '90',
    '-fill', '#10002B', '-gravity', 'center',
    '-draw', "text -4,184 'MuftTools'",
    '-draw', "text 4,184 'MuftTools'",
    '-draw', "text 0,188 'MuftTools'",
    '-fill', 'white',
    '-draw', "text 0,180 'MuftTools'",

    # Subtle Line Divider
    '-stroke', '#A855F7', '-strokewidth', '4',
    '-draw', 'line 390,1210 690,1210',
    '-stroke', 'none',

    # Tagline "Offline All-in-One Utility Toolkit"
    '-font', 'DejaVu-Sans-Bold', '-pointsize', '34',
    '-fill', '#DDD6FE',
    '-draw', "text 0,270 'Offline All-in-One Utility Toolkit'",

    'app/src/main/res/drawable/splash_screen.png'
]

subprocess.run(splash_screen_cmd, check=True)
print('Splash screen HD asset rendered!')
print('All branding assets generated successfully!')
