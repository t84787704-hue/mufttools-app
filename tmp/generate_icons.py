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

print('Generating 1024x1024 Ultra HD Master Icon...')

cmd = [
    'convert', '-size', '1024x1024', 'xc:none',
    
    # Outer 3D Squircle Base Canvas
    '(', '-size', '1024x1024', 'xc:#4C0099', '-draw', 'roundrectangle 16,16 1008,1008 220,220', ')', '-composite',
    '(', '-size', '1024x1024', 'xc:#6B00D7', '-draw', 'roundrectangle 16,16 1008,760 200,200', ')', '-composite',
    '(', '-size', '1024x1024', 'xc:#2A0052', '-draw', 'roundrectangle 16,600 1008,1008 200,200', ')', '-composite',

    # Glowing Rim Edge
    '-stroke', '#A855F7', '-strokewidth', '6', '-fill', 'none',
    '-draw', 'roundrectangle 32,32 992,992 204,204',
    '-stroke', 'none',

    # Dark Interior Cavity
    '-fill', '#160033',
    '-draw', 'roundrectangle 180,200 844,520 60,60',

    # Metallic Wrench Handle in Lid
    '-stroke', '#E2E8F0', '-strokewidth', '32', '-fill', 'none',
    '-draw', 'line 380,240 380,150',
    '-draw', 'line 380,150 644,150',
    '-draw', 'line 644,150 644,240',
    '-stroke', '#94A3B8', '-strokewidth', '16',
    '-draw', 'line 380,240 380,150',
    '-draw', 'line 380,150 644,150',
    '-draw', 'line 644,150 644,240',
    '-stroke', 'none',

    # Badges Inside Toolbox (PDF, Image/Photo, QR Code, Video)
    # A. PDF Badge (Red)
    '(',
      '-size', '180x220', 'xc:none',
      '-fill', 'white', '-draw', 'roundrectangle 0,0 180,220 28,28',
      '-fill', '#DC2626', '-draw', 'roundrectangle 0,0 180,110 28,28',
      '-fill', '#DC2626', '-draw', 'rectangle 0,60 180,110',
      '-font', 'DejaVu-Sans-Bold', '-pointsize', '48', '-fill', 'white',
      '-gravity', 'center', '-draw', "text 0,-25 'PDF'",
      '-fill', '#E2E8F0', '-draw', 'roundrectangle 30,140 150,154 7,7',
      '-fill', '#E2E8F0', '-draw', 'roundrectangle 30,168 110,182 7,7',
      '-rotate', '-12',
    ')', '-geometry', '+130+250', '-composite',

    # B. Gallery Photo Badge (Blue)
    '(',
      '-size', '170x170', 'xc:none',
      '-fill', '#2563EB', '-draw', 'roundrectangle 0,0 170,170 32,32',
      '-fill', 'white', '-draw', 'circle 125,42 125,58',
      '-fill', 'white', '-draw', 'polygon 20,150 70,85 105,125 125,100 150,150',
      '-rotate', '-5',
    ')', '-geometry', '+295+320', '-composite',

    # C. QR Code Badge (White/Dark)
    '(',
      '-size', '170x170', 'xc:none',
      '-fill', 'white', '-draw', 'roundrectangle 0,0 170,170 32,32',
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
      '-rotate', '6',
    ')', '-geometry', '+525+310', '-composite',

    # D. Video Player Badge (Purple)
    '(',
      '-size', '180x180', 'xc:none',
      '-fill', '#7C3AED', '-draw', 'roundrectangle 0,0 180,180 36,36',
      '-stroke', 'white', '-strokewidth', '6', '-fill', 'none',
      '-draw', 'roundrectangle 18,18 162,162 22,22',
      '-stroke', 'none', '-fill', 'white',
      '-draw', 'polygon 70,55 125,90 70,125',
      '-rotate', '14',
    ')', '-geometry', '+675+265', '-composite',

    # Metallic Wrench Lock/Latch in Center
    '(',
      '-size', '200x380', 'xc:none',
      '-fill', '#F1F5F9', '-draw', 'circle 100,75 100,150',
      '-fill', '#160033', '-draw', 'polygon 78,0 122,0 100,75',
      '-fill', '#F1F5F9', '-draw', 'roundrectangle 62,110 138,340 14,14',
      '-fill', '#CBD5E1', '-draw', 'roundrectangle 20,290 180,360 18,18',
      '-fill', '#475569', '-draw', 'roundrectangle 45,315 155,335 8,8',
    ')', '-geometry', '+412+185', '-composite',

    # Main Toolbox Front Body
    '(',
      '-size', '1024x1024', 'xc:none',
      '-fill', '#280054', '-draw', 'roundrectangle 116,480 908,920 52,52',
      '-fill', '#3B007A', '-draw', 'roundrectangle 116,480 908,810 48,48',
      '-fill', '#5C00BD', '-draw', 'roundrectangle 100,455 924,535 40,40',
      '-stroke', '#C084FC', '-strokewidth', '4', '-fill', 'none',
      '-draw', 'roundrectangle 108,463 916,527 32,32',
    ')', '-composite',

    # Sparkles Accent
    '-stroke', '#E9D5FF', '-strokewidth', '8',
    '-draw', 'line 140,640 175,640',
    '-draw', 'line 150,615 180,628',
    '-draw', 'line 150,665 180,652',
    '-draw', 'line 884,640 849,640',
    '-draw', 'line 874,615 844,628',
    '-draw', 'line 874,665 844,652',
    '-stroke', 'none',

    # App Title "Free Tools" Text on Front Panel
    '-font', 'DejaVu-Sans-Bold', '-pointsize', '110',
    '-fill', '#120029', '-gravity', 'center',
    '-draw', "text -6,146 'Free Tools'",
    '-draw', "text 6,146 'Free Tools'",
    '-draw', "text 0,152 'Free Tools'",
    '-fill', 'white',
    '-draw', "text 0,140 'Free Tools'",

    # Subtitle
    '-font', 'DejaVu-Sans-Bold', '-pointsize', '30',
    '-fill', '#22C55E', '-draw', "text -240,230 'OFFLINE'",
    '-fill', 'white', '-draw', "text 110,230 'ALL-in-One UTILITY TOOLS'",

    # Separator Line
    '-stroke', '#C084FC', '-strokewidth', '3',
    '-draw', 'line 195,765 829,765',
    '-stroke', 'none',

    # Bottom 4 Tool Category Box Indicators
    '-font', 'DejaVu-Sans-Bold', '-pointsize', '22', '-fill', 'white',
    '-draw', "text -260,315 'PDF'",
    '-stroke', 'white', '-strokewidth', '3', '-fill', 'none',
    '-draw', 'roundrectangle 215,800 285,870 12,12', # PDF Box
    '-draw', 'line 335,800 335,870',                 # Divider 1
    '-draw', 'roundrectangle 365,800 435,870 12,12', # QR Box
    '-draw', 'line 485,800 485,870',                 # Divider 2
    '-draw', 'roundrectangle 515,800 585,870 12,12', # Photo Box
    '-draw', 'line 635,800 635,870',                 # Divider 3
    '-draw', 'roundrectangle 665,800 735,870 12,12', # Video Box
    '-stroke', 'none',

    '/tmp/playstore_master_hd.png'
]

subprocess.run(cmd, check=True)
print('Master 1024x1024 Play Store Icon rendered successfully!')

# Render both WebP & PNG for launcher mipmap density directories
for density, (w, h) in densities.items():
    out_dir = os.path.join(res_dir, f'mipmap-{density}')
    os.makedirs(out_dir, exist_ok=True)
    
    # 1. Square Launcher WebP & PNG
    ic_webp = os.path.join(out_dir, 'ic_launcher.webp')
    ic_png = os.path.join(out_dir, 'ic_launcher.png')
    subprocess.run(['convert', '/tmp/playstore_master_hd.png', '-resize', f'{w}x{h}', '-quality', '95', ic_webp], check=True)
    subprocess.run(['convert', '/tmp/playstore_master_hd.png', '-resize', f'{w}x{h}', ic_png], check=True)
    
    # 2. Round Launcher WebP & PNG
    ic_round_webp = os.path.join(out_dir, 'ic_launcher_round.webp')
    ic_round_png = os.path.join(out_dir, 'ic_launcher_round.png')
    cmd_round_webp = [
        'convert', '/tmp/playstore_master_hd.png', '-resize', f'{w}x{h}',
        '(', '-size', f'{w}x{h}', 'xc:none', '-fill', 'white', '-draw', f'circle {w//2},{h//2} {w//2},0', ')',
        '-compose', 'DstIn', '-composite',
        '-quality', '95', ic_round_webp
    ]
    cmd_round_png = [
        'convert', '/tmp/playstore_master_hd.png', '-resize', f'{w}x{h}',
        '(', '-size', f'{w}x{h}', 'xc:none', '-fill', 'white', '-draw', f'circle {w//2},{h//2} {w//2},0', ')',
        '-compose', 'DstIn', '-composite',
        ic_round_png
    ]
    subprocess.run(cmd_round_webp, check=True)
    subprocess.run(cmd_round_png, check=True)
    print(f'Generated {density} ({w}x{h}): WebP & PNG launcher icons')

print('All Ultra HD Launcher icon assets generated!')
