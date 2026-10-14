param([string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot))

Add-Type -AssemblyName System.Drawing

$sourcePath = Join-Path $ProjectRoot 'source\launcher_glove_v1.3.10.png'
$resRoot = Join-Path $ProjectRoot 'app\src\main\res'
$reportRoot = Join-Path $ProjectRoot '测试报告'
if(-not (Test-Path -LiteralPath $sourcePath)){ throw "Launcher source not found: $sourcePath" }
New-Item -ItemType Directory -Force -Path (Join-Path $resRoot 'drawable-nodpi'),$reportRoot | Out-Null

$source = [System.Drawing.Bitmap]::FromFile($sourcePath)
$foreground = [System.Drawing.Bitmap]::new(432,432,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($foreground)
$g.Clear([System.Drawing.Color]::Transparent)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half

# Preserve the full user image aspect ratio and place it inside the adaptive-icon
# circular safe region. No crop, stretch, repaint, or generated replacement.
$targetW = 300
$targetH = [int][Math]::Round($targetW * $source.Height / $source.Width)
$targetX = [int]((432-$targetW)/2)
$targetY = [int]((432-$targetH)/2)
$g.DrawImage($source,[System.Drawing.Rectangle]::new($targetX,$targetY,$targetW,$targetH),
    [System.Drawing.Rectangle]::new(0,0,$source.Width,$source.Height),[System.Drawing.GraphicsUnit]::Pixel)
$g.Dispose(); $source.Dispose()

# Convert only the near-black photographic canvas into alpha. Colored neon glow,
# cyan/pink accents, purple shadows and the original pixel edges are retained.
for($y=0;$y -lt 432;$y++){
    for($x=0;$x -lt 432;$x++){
        $c=$foreground.GetPixel($x,$y)
        if($c.A -eq 0){ continue }
        $max=[Math]::Max($c.R,[Math]::Max($c.G,$c.B))
        if($max -le 12){
            $foreground.SetPixel($x,$y,[System.Drawing.Color]::Transparent)
        } elseif($max -lt 48){
            $alpha=[Math]::Min(255,[int](($max-12)*7.1))
            $foreground.SetPixel($x,$y,[System.Drawing.Color]::FromArgb($alpha,$c.R,$c.G,$c.B))
        }
    }
}

$foregroundPath = Join-Path $resRoot 'drawable-nodpi\ic_launcher_foreground.png'
$foreground.Save($foregroundPath,[System.Drawing.Imaging.ImageFormat]::Png)

function New-Composite([bool]$roundMask){
    $bitmap=[System.Drawing.Bitmap]::new(432,432,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $cg=[System.Drawing.Graphics]::FromImage($bitmap)
    $cg.SmoothingMode=[System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $cg.Clear([System.Drawing.Color]::Transparent)
    $bg=[System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,2,7,24))
    if($roundMask){ $cg.FillEllipse($bg,0,0,432,432) } else { $cg.FillRectangle($bg,0,0,432,432) }
    $cg.DrawImage($foreground,0,0,432,432)
    $bg.Dispose(); $cg.Dispose()
    return $bitmap
}

function Save-Scaled([System.Drawing.Bitmap]$sourceBitmap,[string]$path,[int]$size){
    $bitmap=[System.Drawing.Bitmap]::new($size,$size,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $sg=[System.Drawing.Graphics]::FromImage($bitmap)
    $sg.InterpolationMode=[System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $sg.PixelOffsetMode=[System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $sg.DrawImage($sourceBitmap,[System.Drawing.Rectangle]::new(0,0,$size,$size))
    $sg.Dispose(); $bitmap.Save($path,[System.Drawing.Imaging.ImageFormat]::Png); $bitmap.Dispose()
}

$square = New-Composite $false
$round = New-Composite $true
$densitySizes = [ordered]@{'mipmap-mdpi'=48;'mipmap-hdpi'=72;'mipmap-xhdpi'=96;'mipmap-xxhdpi'=144;'mipmap-xxxhdpi'=192}
foreach($entry in $densitySizes.GetEnumerator()){
    $dir=Join-Path $resRoot $entry.Key
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    Save-Scaled $square (Join-Path $dir 'ic_launcher.png') $entry.Value
    Save-Scaled $round (Join-Path $dir 'ic_launcher_round.png') $entry.Value
}

# Side-by-side mask preview: circle and Android-style rounded square.
$preview=[System.Drawing.Bitmap]::new(880,440,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$pg=[System.Drawing.Graphics]::FromImage($preview)
$pg.SmoothingMode=[System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$pg.Clear([System.Drawing.Color]::FromArgb(255,28,31,39))
$maskCircle=[System.Drawing.Drawing2D.GraphicsPath]::new(); $maskCircle.AddEllipse(60,60,320,320)
$maskRound=[System.Drawing.Drawing2D.GraphicsPath]::new(); $maskRound.AddArc(500,60,96,96,180,90); $maskRound.AddArc(724,60,96,96,270,90); $maskRound.AddArc(724,284,96,96,0,90); $maskRound.AddArc(500,284,96,96,90,90); $maskRound.CloseFigure()
$oldClip=$pg.Clip
$pg.SetClip($maskCircle); $pg.DrawImage($square,[System.Drawing.Rectangle]::new(60,60,320,320)); $pg.Clip=$oldClip
$oldClip2=$pg.Clip
$pg.SetClip($maskRound); $pg.DrawImage($square,[System.Drawing.Rectangle]::new(500,60,320,320)); $pg.Clip=$oldClip2
$border=[System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(255,83,93,119),4)
$pg.DrawEllipse($border,60,60,320,320); $pg.DrawPath($border,$maskRound)
$border.Dispose(); $maskCircle.Dispose(); $maskRound.Dispose(); $pg.Dispose()
$preview.Save((Join-Path $reportRoot 'v1.3.10-launcher-遮罩预览.png'),[System.Drawing.Imaging.ImageFormat]::Png)

$preview.Dispose(); $square.Dispose(); $round.Dispose(); $foreground.Dispose()
Write-Output "Generated v1.3.10 adaptive and legacy launcher assets."
