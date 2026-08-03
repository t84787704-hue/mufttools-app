import os
import subprocess

print('Creating 3D Metallic Purple Toolbox + Silver Wrench Splash Screen...')

res_drawable = 'app/src/main/res/drawable'
os.makedirs(res_drawable, exist_ok=True)

# 1. First, create a 1024x1024 Master 3D Metallic Toolbox Logo with Silver Wrench (NO small tool icons)
logo_cmd = [
    'convert', '-size', '1024x1024', 'xc:none',

    # Soft ambient drop shadow under base
    '(', '-size', '1024x1024', 'xc:none',
    '-fill', '#0A0318A0', '-draw', 'ellipse 512,890 380,70 0,360',
    '-blur', '0x20', ')', '-composite',

    # Toolbox Back Lid Recess
    '(', '-size', '1024x1024', 'xc:none',
    '-fill', '#12042B', '-draw', 'roundrectangle 210,220 814,500 50,50',
    ')', '-composite',

    # Chrome Metallic Wrench Handle / Top Carry Bar
    '-stroke', '#F8FAFC', '-strokewidth', '36', '-fill', 'none',
    '-draw', 'line 380,240 380,140',
    '-draw', 'line 380,140 644,140',
    '-draw', 'line 644,140 644,240',
    '-stroke', '#64748B', '-strokewidth', '18',
    '-draw', 'line 380,240 380,140',
    '-draw', 'line 380,140 644,140',
    '-draw', 'line 644,140 644,240',
    '-stroke', 'none',

    # Chrome Wrench Body in Lid Cavity (Large Silver Metallic Wrench)
    '(',
      '-size', '320x420', 'xc:none',
      # Wrench Open Head
      '-fill', '#F8FAFC', '-draw', 'circle 160,110 160,200',
      '-fill', '#12042B', '-draw', 'polygon 125,10 195,10 160,110',
      '-fill', '#E2E8F0', '-draw', 'circle 160,110 160,160',
      '-fill', '#12042B', '-draw', 'circle 160,110 160,130',
      # Wrench Shaft
      '-fill', '#CBD5E1', '-draw', 'roundrectangle 130,160 190,400 16,16',
      '-fill', '#64748B', '-draw', 'roundrectangle 142,180 178,380 10,10',
      '-fill', '#E2E8F0', '-draw', 'roundrectangle 148,190 172,370 6,6',
      '-rotate', '-35',
    ')', '-geometry', '+260+130', '-composite',

    # 3D Metallic Purple Toolbox Front Chest Body
    '(',
      '-size', '1024x1024', 'xc:none',
      # Body Base Fill
      '-fill', '#2E0B5A', '-draw', 'roundrectangle 120,460 904,900 56,56',
      # Top Half Shading
      '-fill', '#5B21B6', '-draw', 'roundrectangle 120,460 904,760 48,48',
      # Glowing Beveled Top Rim
      '-fill', '#7C3AED', '-draw', 'roundrectangle 100,430 924,510 38,38',
      '-stroke', '#DDD6FE', '-strokewidth', '5', '-fill', 'none',
      '-draw', 'roundrectangle 108,438 916,502 30,30',
      '-stroke', 'none',
    ')', '-composite',

    # Center Metallic Latch Plate (Chrome / Silver)
    '(',
      '-size', '160x240', 'xc:none',
      '-fill', '#F8FAFC', '-draw', 'roundrectangle 10,10 150,230 26,26',
      '-fill', '#475569', '-draw', 'roundrectangle 32,40 128,150 14,14',
      '-fill', '#0284C7', '-draw', 'circle 80,190 80,208',
      '-fill', '#E2E8F0', '-draw', 'circle 80,190 80,198',
    ')', '-geometry', '+432+510', '-composite',

    # Horizontal Chrome Accent Strips across front chest
    '-stroke', '#A855F7', '-strokewidth', '4',
    '-draw', 'line 160,680 864,680',
    '-draw', 'line 160,740 864,740',
    '-stroke', 'none',

    # Bottom Corner Chrome Guards
    '-fill', '#F1F5F9',
    '-draw', 'roundrectangle 120,840 210,900 20,20',
    '-draw', 'roundrectangle 814,840 904,900 20,20',

    'app/src/main/res/drawable/splash_logo.png'
]

subprocess.run(logo_cmd, check=True)
print('Splash Logo (1024x1024) created successfully!')

# 2. Now render the full 1080x1920 Ultra HD Splash Screen
splash_cmd = [
    'convert', '-size', '1080x1920', 'xc:#1A1038',

    # Subtle Radial Purple Glow behind logo
    '(', '-size', '1080x1920', 'xc:none',
    '-fill', '#5B21B6', '-draw', 'circle 540,700 540,250',
    '-blur', '0x120', ')', '-composite',

    '(', '-size', '1080x1920', 'xc:none',
    '-fill', '#7C3AED80', '-draw', 'circle 540,700 540,420',
    '-blur', '0x80', ')', '-composite',

    # Centered Large 3D Toolbox Logo (scaled to approx 640x640 ~ 40% height)
    '(', 'app/src/main/res/drawable/splash_logo.png', '-resize', '660x660', ')',
    '-geometry', '+210+340', '-composite',

    # "Free Tools" Main Title
    '-font', 'DejaVu-Sans-Bold', '-pointsize', '100',
    '-fill', '#090314', '-gravity', 'center',
    '-draw', "text -4,224 'Free Tools'",
    '-draw', "text 4,224 'Free Tools'",
    '-draw', "text 0,228 'Free Tools'",
    '-fill', '#FFFFFF',
    '-draw', "text 0,220 'Free Tools'",

    # "Offline All-in-One Utility Tools" Subtitle
    '-font', 'DejaVu-Sans-Bold', '-pointsize', '36',
    '-fill', '#090314',
    '-draw', "text -2,322 'Offline All-in-One Utility Tools'",
    '-draw', "text 2,322 'Offline All-in-One Utility Tools'",
    '-fill', '#DDD6FE',
    '-draw', "text 0,320 'Offline All-in-One Utility Tools'",

    'app/src/main/res/drawable/splash_screen.png'
]

subprocess.run(splash_cmd, check=True)
print('Ultra HD Splash Screen (1080x1920) rendered successfully!')
