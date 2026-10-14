param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

Add-Type -AssemblyName System.Drawing

$sourcePath = Join-Path $ProjectRoot 'source\Codex 图像 2026年9月23日 16_12_42.png'
$outputDir = Join-Path $ProjectRoot 'app\src\main\res\drawable-nodpi'
if (-not (Test-Path -LiteralPath $sourcePath)) { throw "Reference image not found: $sourcePath" }
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$source = [System.Drawing.Bitmap]::FromFile($sourcePath)
$pixel = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor

function New-Canvas([int]$width, [int]$height) {
    return [System.Drawing.Bitmap]::new($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

function Save-Png([System.Drawing.Bitmap]$bitmap, [string]$name) {
    $path = Join-Path $outputDir $name
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function Crop-Asset([int]$x, [int]$y, [int]$width, [int]$height) {
    $bitmap = New-Canvas $width $height
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.InterpolationMode = $pixel
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(0, 0, $width, $height),
        [System.Drawing.Rectangle]::new($x, $y, $width, $height), [System.Drawing.GraphicsUnit]::Pixel)
    $graphics.Dispose()
    return $bitmap
}

function Clear-DarkBackground([System.Drawing.Bitmap]$bitmap, [string]$mode) {
    for ($y = 0; $y -lt $bitmap.Height; $y++) {
        for ($x = 0; $x -lt $bitmap.Width; $x++) {
            $color = $bitmap.GetPixel($x, $y)
            $max = [Math]::Max($color.R, [Math]::Max($color.G, $color.B))
            $keep = $true
            if ($mode -eq 'yellowRed') {
                $keep = (($color.R -gt 155 -and $color.G -gt 85 -and $color.B -lt 125) -or
                         ($color.R -gt 110 -and $color.R -gt ($color.B * 1.25)))
            } elseif ($mode -eq 'redWhite') {
                $keep = (($color.R -gt 125 -and $color.R -gt ($color.B * 1.18)) -or
                         ($color.R -gt 205 -and $color.G -gt 205 -and $color.B -gt 195))
            }
            if (-not $keep -or $max -lt 45) {
                $bitmap.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
            }
        }
    }
}

function Clear-TopWhiteFragments([System.Drawing.Bitmap]$bitmap, [int]$cutoffY) {
    for ($y = 0; $y -lt [Math]::Min($cutoffY, $bitmap.Height); $y++) {
        for ($x = 0; $x -lt $bitmap.Width; $x++) {
            $color = $bitmap.GetPixel($x, $y)
            if ($color.A -gt 0 -and $color.R -gt 175 -and $color.G -gt 175 -and $color.B -gt 165) {
                $bitmap.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
            }
        }
    }
}

function Fill-Rect([System.Drawing.Bitmap]$bitmap, [int]$x, [int]$y, [int]$w, [int]$h, [System.Drawing.Color]$color) {
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $brush = [System.Drawing.SolidBrush]::new($color)
    $g.FillRectangle($brush, $x, $y, $w, $h)
    $brush.Dispose(); $g.Dispose()
}

function Clear-Rect([System.Drawing.Bitmap]$bitmap, [int]$x, [int]$y, [int]$w, [int]$h) {
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
    $brush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::Transparent)
    $g.FillRectangle($brush, $x, $y, $w, $h)
    $brush.Dispose(); $g.Dispose()
}

function Draw-PixelBorder([System.Drawing.Graphics]$g, [int]$x, [int]$y, [int]$w, [int]$h, [System.Drawing.Color]$fill, [System.Drawing.Color]$light, [System.Drawing.Color]$dark) {
    $fillBrush = [System.Drawing.SolidBrush]::new($fill)
    $lightBrush = [System.Drawing.SolidBrush]::new($light)
    $darkBrush = [System.Drawing.SolidBrush]::new($dark)
    $g.FillRectangle($darkBrush, $x + 7, $y + 7, $w, $h)
    $g.FillRectangle($fillBrush, $x, $y, $w, $h)
    $g.FillRectangle($lightBrush, $x, $y, $w, 5)
    $g.FillRectangle($lightBrush, $x, $y, 5, $h)
    $g.FillRectangle($darkBrush, $x, $y + $h - 7, $w, 7)
    $g.FillRectangle($darkBrush, $x + $w - 7, $y, 7, $h)
    $fillBrush.Dispose(); $lightBrush.Dispose(); $darkBrush.Dispose()
}

function Draw-CenteredPixelText([System.Drawing.Graphics]$g, [string]$text, [System.Drawing.RectangleF]$bounds,
    [float]$size, [System.Drawing.Color]$fill, [System.Drawing.Color]$outline, [float]$stroke) {
    $family = [System.Drawing.FontFamily]::new('Microsoft YaHei UI')
    $format = [System.Drawing.StringFormat]::new()
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $path.AddString($text, $family, [int][System.Drawing.FontStyle]::Bold, $size, $bounds, $format)
    $pen = [System.Drawing.Pen]::new($outline, $stroke)
    $pen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Miter
    $brush = [System.Drawing.SolidBrush]::new($fill)
    $g.DrawPath($pen, $path)
    $g.FillPath($brush, $path)
    $family.Dispose(); $format.Dispose(); $path.Dispose(); $pen.Dispose(); $brush.Dispose()
}

function Draw-ArrowButton([System.Drawing.Graphics]$g, [int]$x, [int]$y, [bool]$up) {
    Draw-PixelBorder $g $x $y 64 34 ([System.Drawing.Color]::FromArgb(255,25,112,213)) ([System.Drawing.Color]::FromArgb(255,113,207,255)) ([System.Drawing.Color]::FromArgb(255,2,22,63))
    $arrow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
    if ($up) {
        $points = @([System.Drawing.Point]::new($x+32,$y+8),[System.Drawing.Point]::new($x+19,$y+24),[System.Drawing.Point]::new($x+45,$y+24))
    } else {
        $points = @([System.Drawing.Point]::new($x+19,$y+10),[System.Drawing.Point]::new($x+45,$y+10),[System.Drawing.Point]::new($x+32,$y+26))
    }
    $g.FillPolygon($arrow,$points)
    $arrow.Dispose()
}

function New-SettingsPanel {
    $bitmap = New-Canvas 493 427
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.Clear([System.Drawing.Color]::Transparent)
    Draw-PixelBorder $g 3 3 487 420 ([System.Drawing.Color]::FromArgb(255,44,105,150)) ([System.Drawing.Color]::FromArgb(255,156,218,249)) ([System.Drawing.Color]::FromArgb(255,2,18,46))
    $inner = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,7,35,66))
    $g.FillRectangle($inner,12,12,469,400)
    $labels = @('训练时间','休息时间','回合数','倒计时提醒')
    for ($row=0; $row -lt 4; $row++) {
        $rowY = 16 + $row * 98
        Draw-PixelBorder $g 16 $rowY 461 92 ([System.Drawing.Color]::FromArgb(255,24,67,104)) ([System.Drawing.Color]::FromArgb(255,122,197,239)) ([System.Drawing.Color]::FromArgb(255,2,17,43))
        $labelBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,11,48,82))
        $valueBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,1,10,22))
        $g.FillRectangle($labelBrush,22,$rowY+9,178,70)
        $g.FillRectangle($valueBrush,202,$rowY+9,190,70)
        $cyan = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,47,166,229))
        $g.FillRectangle($cyan,202,$rowY+9,3,70)
        $g.FillRectangle($cyan,202,$rowY+76,190,3)
        $labelBrush.Dispose(); $valueBrush.Dispose(); $cyan.Dispose()
        Draw-ArrowButton $g 401 ($rowY+8) $true
        Draw-ArrowButton $g 401 ($rowY+49) $false
        $fontSize = $(if ($row -eq 3) { 25 } else { 31 })
        Draw-CenteredPixelText $g $labels[$row] ([System.Drawing.RectangleF]::new(24,$rowY+8,174,72)) $fontSize ([System.Drawing.Color]::White) ([System.Drawing.Color]::FromArgb(255,1,12,31)) 3
    }
    $inner.Dispose(); $g.Dispose()
    return $bitmap
}

function New-SimpleCrown {
    $bitmap = New-Canvas 90 65
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.Clear([System.Drawing.Color]::Transparent)
    $shadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,91,45,8))
    $gold = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,255,224,54))
    $light = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,255,249,116))
    $outline = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(255,3,17,43),4)
    [System.Drawing.Point[]]$points = @([System.Drawing.Point]::new(10,43),[System.Drawing.Point]::new(10,17),[System.Drawing.Point]::new(27,33),[System.Drawing.Point]::new(45,7),[System.Drawing.Point]::new(63,33),[System.Drawing.Point]::new(80,17),[System.Drawing.Point]::new(80,43))
    [System.Drawing.Point[]]$shadowPoints = $points | ForEach-Object { [System.Drawing.Point]::new($_.X+4,$_.Y+4) }
    $g.FillPolygon($shadow,$shadowPoints); $g.FillPolygon($gold,$points); $g.DrawPolygon($outline,$points)
    $g.FillRectangle($gold,10,43,70,12); $g.DrawRectangle($outline,10,43,70,12)
    $g.FillRectangle($light,17,46,56,3)
    $shadow.Dispose(); $gold.Dispose(); $light.Dispose(); $outline.Dispose(); $g.Dispose()
    return $bitmap
}

function New-TextAsset([int]$width,[int]$height,[string]$text,[float]$size,[System.Drawing.Color]$fill,[System.Drawing.Color]$outline,[float]$stroke) {
    $bitmap = New-Canvas $width $height
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.Clear([System.Drawing.Color]::Transparent)
    Draw-CenteredPixelText $g $text ([System.Drawing.RectangleF]::new(2,2,$width-4,$height-4)) $size $fill $outline $stroke
    $g.Dispose()
    return $bitmap
}

function Repair-PhoneCutout([System.Drawing.Graphics]$g) {
    # Replace only the central phone-notch/camera zone with a deliberately built
    # pixel roof truss. The irregular braces avoid a mirrored or blurred patch.
    $deep = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,3,13,38))
    $navy = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,5,27,65))
    $steel = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,10,54,112))
    $blue = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,18,91,178))
    $glint = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,37,132,227))

    [System.Drawing.Point[]]$roof = @(
        [System.Drawing.Point]::new(202,0), [System.Drawing.Point]::new(338,0),
        [System.Drawing.Point]::new(356,64), [System.Drawing.Point]::new(184,64)
    )
    $g.FillPolygon($deep,$roof)
    $g.FillRectangle($navy,202,0,136,8)
    $g.FillRectangle($steel,199,10,142,4)
    $g.FillRectangle($navy,197,15,146,7)
    $g.FillRectangle($blue,194,23,152,4)
    $g.FillRectangle($navy,192,28,156,7)
    $g.FillRectangle($steel,190,37,160,4)
    $g.FillRectangle($navy,188,42,164,8)
    $g.FillRectangle($blue,186,53,168,4)
    $g.FillRectangle($navy,184,58,172,6)

    [System.Drawing.Point[][]]$braces = @(
        @([System.Drawing.Point]::new(215,14),[System.Drawing.Point]::new(221,14),[System.Drawing.Point]::new(211,58),[System.Drawing.Point]::new(204,58)),
        @([System.Drawing.Point]::new(241,14),[System.Drawing.Point]::new(247,14),[System.Drawing.Point]::new(243,58),[System.Drawing.Point]::new(236,58)),
        @([System.Drawing.Point]::new(267,14),[System.Drawing.Point]::new(273,14),[System.Drawing.Point]::new(274,58),[System.Drawing.Point]::new(267,58)),
        @([System.Drawing.Point]::new(293,14),[System.Drawing.Point]::new(299,14),[System.Drawing.Point]::new(305,58),[System.Drawing.Point]::new(298,58)),
        @([System.Drawing.Point]::new(319,14),[System.Drawing.Point]::new(325,14),[System.Drawing.Point]::new(337,58),[System.Drawing.Point]::new(330,58))
    )
    foreach($brace in $braces){ $g.FillPolygon($steel,$brace) }
    $g.FillRectangle($glint,229,24,6,3)
    $g.FillRectangle($glint,278,38,7,3)
    $g.FillRectangle($glint,324,24,5,3)

    $deep.Dispose(); $navy.Dispose(); $steel.Dispose(); $blue.Dispose(); $glint.Dispose()
}

function New-ArenaBackground([bool]$timer) {
    $bitmap = New-Canvas 540 1120
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.InterpolationMode = $pixel
    $g.Clear([System.Drawing.Color]::FromArgb(255, 1, 11, 35))

    if ($timer) {
        $headerSource = [System.Drawing.Rectangle]::new(729, 47, 522, 274)
        $g.DrawImage($source, [System.Drawing.Rectangle]::new(0, 0, 540, 283), $headerSource, [System.Drawing.GraphicsUnit]::Pixel)
    } else {
        $headerSource = [System.Drawing.Rectangle]::new(80, 47, 524, 340)
        $g.DrawImage($source, [System.Drawing.Rectangle]::new(0, 0, 540, 350), $headerSource, [System.Drawing.GraphicsUnit]::Pixel)
    }
    Repair-PhoneCutout $g

    $navy = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 2, 20, 58))
    $deep = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 1, 10, 31))
    $blue = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 12, 69, 145))
    $cyan = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 35, 146, 236))
    $red = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 235, 77, 70))
    $gold = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 255, 220, 54))
    $arenaFillTop = $(if ($timer) { 275 } else { 350 })
    $g.FillRectangle($deep, 0, $arenaFillTop, 540, 1120-$arenaFillTop)
    for ($band = 0; $band -lt 8; $band++) {
        $yy = $arenaFillTop + 10 + $band * 76
        $g.FillRectangle($(if ($band % 2 -eq 0) { $navy } else { $deep }), 0, $yy, 540, 76)
        $g.FillRectangle($blue, 0, $yy, 540, 4)
    }
    for ($y = $arenaFillTop + 25; $y -lt 960; $y += 13) {
        for ($x = 5; $x -lt 535; $x += 11) {
            $selector = (($x * 7 + $y * 11) / 11) % 17
            if ($selector -lt 1) { $brush = $red }
            elseif ($selector -lt 3) { $brush = $cyan }
            elseif ($selector -lt 7) { $brush = $blue }
            else { $brush = $navy }
            $g.FillRectangle($brush, $x, $y, 4, 5)
        }
    }
    if ($timer) { for ($x = 0; $x -lt 540; $x += 24) { $g.FillRectangle($(if (($x / 24) % 2 -eq 0) { $gold } else { $deep }), $x, 268, 12, 11) } }
    $beamBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(45, 64, 169, 255))
    if ($timer) {
        $g.FillPolygon($beamBrush, @([System.Drawing.Point]::new(118, 65), [System.Drawing.Point]::new(140, 65), [System.Drawing.Point]::new(260, 850), [System.Drawing.Point]::new(210, 850)))
        $g.FillPolygon($beamBrush, @([System.Drawing.Point]::new(402, 65), [System.Drawing.Point]::new(424, 65), [System.Drawing.Point]::new(330, 850), [System.Drawing.Point]::new(280, 850)))
    }
    if ($timer) {
        $starBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 255, 223, 75))
        $g.FillRectangle($starBrush, 267, 72, 7, 118)
        $g.FillRectangle($starBrush, 214, 127, 112, 7)
        $g.FillRectangle($starBrush, 235, 94, 7, 82)
        $g.FillRectangle($starBrush, 299, 94, 7, 82)
        $starBrush.Dispose()
        $floorDark = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,12,70,132))
        $floorLight = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,19,105,177))
        $g.FillRectangle($floorDark, 0, 950, 540, 170)
        $g.FillRectangle($floorLight,0,950,540,8)
        $floorDark.Dispose(); $floorLight.Dispose()
    }
    $navy.Dispose(); $deep.Dispose(); $blue.Dispose(); $cyan.Dispose(); $red.Dispose(); $gold.Dispose(); $beamBrush.Dispose(); $g.Dispose()
    return $bitmap
}

function New-TimerRing {
    $bitmap = New-Canvas 540 280
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.Clear([System.Drawing.Color]::Transparent)
    $floor = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 25, 127, 203))
    $tile = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 43, 152, 222))
    $red = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 239, 57, 66))
    $white = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
    $blue = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 27, 112, 218))
    $dark = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 1, 12, 33))
    $g.FillRectangle($floor, 0, 120, 540, 160)
    for ($y = 0; $y -lt 5; $y++) { if ($y % 2 -eq 0) { $g.FillRectangle($tile,0,120+$y*32,540,14) } }
    $g.FillRectangle($dark, 0, 51, 540, 7); $g.FillRectangle($red, 0, 55, 540, 8)
    $g.FillRectangle($dark, 0, 82, 540, 7); $g.FillRectangle($white, 0, 86, 540, 8)
    $g.FillRectangle($dark, 0, 113, 540, 7); $g.FillRectangle($blue, 0, 117, 540, 8)
    Draw-PixelBorder $g 18 28 35 150 ([System.Drawing.Color]::FromArgb(255,35,136,229)) ([System.Drawing.Color]::FromArgb(255,109,205,255)) ([System.Drawing.Color]::FromArgb(255,2,24,65))
    Draw-PixelBorder $g 487 28 35 150 ([System.Drawing.Color]::FromArgb(255,35,136,229)) ([System.Drawing.Color]::FromArgb(255,109,205,255)) ([System.Drawing.Color]::FromArgb(255,2,24,65))
    $floor.Dispose(); $tile.Dispose(); $red.Dispose(); $white.Dispose(); $blue.Dispose(); $dark.Dispose(); $g.Dispose()
    return $bitmap
}

function New-TimerStatusPanel {
    $bitmap = New-Canvas 456 140
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.Clear([System.Drawing.Color]::Transparent)

    # One connected inverted-trapezoid panel: cyan top/side light, steel-blue body,
    # deep lower shadow, and a dark scanline face. It contains no baked status text.
    [System.Drawing.Point[]]$shadowPoints = @(
        [System.Drawing.Point]::new(6,7), [System.Drawing.Point]::new(450,7),
        [System.Drawing.Point]::new(428,140), [System.Drawing.Point]::new(28,140)
    )
    [System.Drawing.Point[]]$outerPoints = @(
        [System.Drawing.Point]::new(0,0), [System.Drawing.Point]::new(456,0),
        [System.Drawing.Point]::new(432,134), [System.Drawing.Point]::new(24,134)
    )
    [System.Drawing.Point[]]$bodyPoints = @(
        [System.Drawing.Point]::new(5,5), [System.Drawing.Point]::new(451,5),
        [System.Drawing.Point]::new(427,129), [System.Drawing.Point]::new(29,129)
    )
    [System.Drawing.Point[]]$facePoints = @(
        [System.Drawing.Point]::new(12,12), [System.Drawing.Point]::new(444,12),
        [System.Drawing.Point]::new(421,122), [System.Drawing.Point]::new(35,122)
    )
    $shadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,1,10,29))
    $cyan = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,71,226,246))
    $steel = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,15,92,129))
    $face = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,3,25,51))
    $g.FillPolygon($shadow,$shadowPoints)
    $g.FillPolygon($cyan,$outerPoints)
    $g.FillPolygon($steel,$bodyPoints)
    $g.FillPolygon($face,$facePoints)

    $scan = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,5,38,70))
    for($yy=17; $yy -lt 121; $yy+=6) {
        $inset = 14 + [int](($yy-12)*.18)
        $g.FillRectangle($scan,$inset,$yy,456-$inset*2,2)
    }
    $lineShadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,0,10,29))
    $line = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,74,226,245))
    $g.FillRectangle($lineShadow,45,72,102,6); $g.FillRectangle($lineShadow,309,72,102,6)
    $g.FillRectangle($line,45,68,102,5); $g.FillRectangle($line,309,68,102,5)
    $g.FillRectangle($line,42,67,5,7); $g.FillRectangle($line,409,67,5,7)

    $shadow.Dispose(); $cyan.Dispose(); $steel.Dispose(); $face.Dispose();
    $scan.Dispose(); $lineShadow.Dispose(); $line.Dispose(); $g.Dispose()
    return $bitmap
}

function New-TimerControlsDock {
    $bitmap = New-Canvas 540 196
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.InterpolationMode = $pixel
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $g.DrawImage($source, [System.Drawing.Rectangle]::new(0,0,540,196),
        [System.Drawing.Rectangle]::new(728,930,524,190), [System.Drawing.GraphicsUnit]::Pixel)

    # Remove the baked labels inside the original button faces while preserving
    # the shared floor and every outer bevel pixel across the full-width dock.
    for($yy=0; $yy -lt 68; $yy++) {
        $blueR = 8; $blueG = 117 - [int]($yy*.22); $blueB = 211 - [int]($yy*.30)
        $redR = 226 - [int]($yy*.18); $redG = 70 - [int]($yy*.15); $redB = 65 - [int]($yy*.14)
        $blueFace = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(255,$blueR,$blueG,$blueB),1)
        $redFace = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(255,$redR,$redG,$redB),1)
        $g.DrawLine($blueFace,55,52+$yy,218,52+$yy)
        $g.DrawLine($redFace,309,52+$yy,477,52+$yy)
        $blueFace.Dispose(); $redFace.Dispose()
    }
    $shine = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(255,96,203,255),2)
    $redShine = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(255,255,141,126),2)
    $g.DrawLine($shine,58,54,214,54); $g.DrawLine($redShine,312,54,473,54)
    $shine.Dispose(); $redShine.Dispose(); $g.Dispose()
    return $bitmap
}

# Backgrounds.
Save-Png (New-ArenaBackground $false) 'ui_home_background.png'
Save-Png (New-ArenaBackground $true) 'ui_timer_background.png'

# Independent reference-derived pieces.
$title = Crop-Asset 132 157 430 132; Clear-DarkBackground $title 'yellowRed'; Save-Png $title 'ui_home_title.png'
Save-Png (New-SimpleCrown) 'ui_crown.png'
$gloves = Crop-Asset 263 255 155 130; Clear-DarkBackground $gloves 'redWhite'; Save-Png $gloves 'ui_gloves.png'
Save-Png (New-SettingsPanel) 'ui_home_settings_panel.png'
Save-Png (Crop-Asset 116 818 468 122) 'ui_home_start_button.png'
Save-Png (Crop-Asset 80 942 525 194) 'ui_ring_footer.png'

$round = Crop-Asset 779 278 420 112
Fill-Rect $round 65 21 294 71 ([System.Drawing.Color]::FromArgb(255,9,43,76))
Save-Png $round 'ui_timer_round_panel.png'

$timerPanel = Crop-Asset 738 374 488 302
Fill-Rect $timerPanel 27 32 435 163 ([System.Drawing.Color]::FromArgb(255,1,9,19))
for ($yy = 38; $yy -lt 190; $yy += 6) { Fill-Rect $timerPanel 30 $yy 429 2 ([System.Drawing.Color]::FromArgb(255,3,19,39)) }
Clear-Rect $timerPanel 0 193 488 109
Save-Png $timerPanel 'ui_timer_panel.png'
Save-Png (New-TimerStatusPanel) 'ui_timer_status_panel.png'

$boxer = Crop-Asset 728 646 524 300
Save-Png $boxer 'ui_boxer_back.png'

$pause = Crop-Asset 747 946 226 132
Fill-Rect $pause 34 34 158 66 ([System.Drawing.Color]::FromArgb(255,8,117,211))
Save-Png $pause 'ui_pause_button.png'
$end = Crop-Asset 994 946 230 132
Fill-Rect $end 34 34 163 66 ([System.Drawing.Color]::FromArgb(255,218,61,55))
Save-Png $end 'ui_end_button.png'
Save-Png (New-TimerRing) 'ui_timer_ring.png'
Save-Png (New-TimerControlsDock) 'ui_timer_controls_dock.png'

Save-Png (New-TextAsset 260 70 '训练中' 43 ([System.Drawing.Color]::FromArgb(255,124,235,255)) ([System.Drawing.Color]::FromArgb(255,1,18,45)) 4) 'ui_phase_training.png'
Save-Png (New-TextAsset 260 70 '休息中' 43 ([System.Drawing.Color]::FromArgb(255,112,244,189)) ([System.Drawing.Color]::FromArgb(255,1,18,45)) 4) 'ui_phase_rest.png'
Save-Png (New-TextAsset 260 70 '已暂停' 43 ([System.Drawing.Color]::White) ([System.Drawing.Color]::FromArgb(255,1,18,45)) 4) 'ui_phase_paused.png'
Save-Png (New-TextAsset 145 70 '暂停' 37 ([System.Drawing.Color]::White) ([System.Drawing.Color]::FromArgb(255,1,23,59)) 4) 'ui_label_pause.png'
Save-Png (New-TextAsset 145 70 '继续' 37 ([System.Drawing.Color]::White) ([System.Drawing.Color]::FromArgb(255,1,23,59)) 4) 'ui_label_resume.png'
Save-Png (New-TextAsset 190 70 '结束训练' 26 ([System.Drawing.Color]::White) ([System.Drawing.Color]::FromArgb(255,74,10,16)) 4) 'ui_label_end.png'

$source.Dispose()
Write-Output "Generated v1.3.10 assets in $outputDir"
