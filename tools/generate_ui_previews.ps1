param([string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot))

Add-Type -AssemblyName System.Drawing
$res = Join-Path $ProjectRoot 'app\src\main\res\drawable-nodpi'
$report = Join-Path $ProjectRoot '测试报告'
New-Item -ItemType Directory -Force -Path $report | Out-Null

function Load([string]$name) { return [System.Drawing.Bitmap]::FromFile((Join-Path $res $name)) }
function DrawAsset($g, [string]$name, [int]$x, [int]$y, [int]$w, [int]$h) {
    $bitmap = Load $name
    $g.DrawImage($bitmap, [System.Drawing.Rectangle]::new($x,$y,$w,$h))
    $bitmap.Dispose()
}
function DrawAssetAlphaCentered($g, [string]$name, [float]$centerX, [float]$centerY) {
    $bitmap = Load $name
    $left=$bitmap.Width; $top=$bitmap.Height; $right=-1; $bottom=-1
    for($yy=0; $yy -lt $bitmap.Height; $yy++) {
        for($xx=0; $xx -lt $bitmap.Width; $xx++) {
            if($bitmap.GetPixel($xx,$yy).A -ne 0) {
                if($xx -lt $left){$left=$xx}; if($xx -gt $right){$right=$xx}
                if($yy -lt $top){$top=$yy}; if($yy -gt $bottom){$bottom=$yy}
            }
        }
    }
    if($right -lt $left -or $bottom -lt $top) { $left=0; $top=0; $right=$bitmap.Width-1; $bottom=$bitmap.Height-1 }
    $alphaCenterX=($left+$right+1)*.5; $alphaCenterY=($top+$bottom+1)*.5
    $g.DrawImage($bitmap,[System.Drawing.RectangleF]::new(
        $centerX-$alphaCenterX,$centerY-$alphaCenterY,$bitmap.Width,$bitmap.Height))
    $bitmap.Dispose()
}
function DrawCentered($g, [string]$text, [float]$size, [System.Drawing.Color]$color, [int]$x, [int]$y, [int]$w, [int]$h, [string]$fontName='Microsoft YaHei UI') {
    $font = [System.Drawing.Font]::new($fontName, $size, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $brush = [System.Drawing.SolidBrush]::new($color)
    $shadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,2,10,24))
    $format = [System.Drawing.StringFormat]::new()
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString($text, $font, $shadow, [System.Drawing.RectangleF]::new($x+3,$y+3,$w,$h), $format)
    $g.DrawString($text, $font, $brush, [System.Drawing.RectangleF]::new($x,$y,$w,$h), $format)
    $font.Dispose(); $brush.Dispose(); $shadow.Dispose(); $format.Dispose()
}
function NewPreview {
    $bitmap = [System.Drawing.Bitmap]::new(540,1120,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    return @($bitmap,$g)
}

function Draw-Segment($g,[float]$x,[float]$y,[float]$w,[float]$h,[bool]$active,[bool]$horizontal) {
    $color = $(if($active){[System.Drawing.Color]::FromArgb(255,255,241,50)}else{[System.Drawing.Color]::FromArgb(255,13,30,35)})
    $brush = [System.Drawing.SolidBrush]::new($color)
    $cut = [Math]::Min($w,$h) * .36
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    if($horizontal) {
        $path.AddPolygon([System.Drawing.PointF[]]@(
            [System.Drawing.PointF]::new($x+$cut,$y),
            [System.Drawing.PointF]::new($x+$w-$cut,$y),
            [System.Drawing.PointF]::new($x+$w,$y+$h*.5),
            [System.Drawing.PointF]::new($x+$w-$cut,$y+$h),
            [System.Drawing.PointF]::new($x+$cut,$y+$h),
            [System.Drawing.PointF]::new($x,$y+$h*.5)
        ))
    } else {
        $path.AddPolygon([System.Drawing.PointF[]]@(
            [System.Drawing.PointF]::new($x+$w*.5,$y),
            [System.Drawing.PointF]::new($x+$w,$y+$cut),
            [System.Drawing.PointF]::new($x+$w,$y+$h-$cut),
            [System.Drawing.PointF]::new($x+$w*.5,$y+$h),
            [System.Drawing.PointF]::new($x,$y+$h-$cut),
            [System.Drawing.PointF]::new($x,$y+$cut)
        ))
    }
    $g.FillPath($brush,$path)
    if($active) {
        $highlight = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,255,255,151))
        $g.FillRectangle($highlight,$x+$w*.18,$y+$h*.12,$w*.36,$h*.16)
        $highlight.Dispose()
    }
    $path.Dispose()
    $brush.Dispose()
}
function Draw-SevenSegmentTime($g,[string]$text,[float]$centerX,[float]$centerY,[float]$height) {
    $maps=@('1111110','0110000','1101101','1111001','0110011','1011011','1011111','1110000','1111111','1111011')
    $dw=$height*.50; $cw=$height*.18; $gap=$height*.055
    $total=$dw*4+$cw+$gap*4; $x=$centerX-$total/2; $top=$centerY-$height/2; $t=$dw*.17
    foreach($ch in $text.ToCharArray()) {
        if($ch -eq ':') {
            $dot=[System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,255,241,50)); $s=$cw*.48
            $g.FillRectangle($dot,$x+$cw*.25,$top+$height*.28,$s,$s); $g.FillRectangle($dot,$x+$cw*.25,$top+$height*.66,$s,$s); $dot.Dispose(); $x += $cw+$gap
        } else {
            $m=$maps[[int]::Parse($ch)]
            Draw-Segment $g ($x+$t*.45) $top ($dw-$t*.9) $t ($m[0]-eq '1') $true
            Draw-Segment $g ($x+$dw-$t) ($top+$t*.55) $t ($height*.38) ($m[1]-eq '1') $false
            Draw-Segment $g ($x+$dw-$t) ($top+$height*.53) $t ($height*.38) ($m[2]-eq '1') $false
            Draw-Segment $g ($x+$t*.45) ($top+$height-$t) ($dw-$t*.9) $t ($m[3]-eq '1') $true
            Draw-Segment $g $x ($top+$height*.53) $t ($height*.38) ($m[4]-eq '1') $false
            Draw-Segment $g $x ($top+$t*.55) $t ($height*.38) ($m[5]-eq '1') $false
            Draw-Segment $g ($x+$t*.45) ($top+$height*.46) ($dw-$t*.9) $t ($m[6]-eq '1') $true
            $x += $dw+$gap
        }
    }
}

$homePair = NewPreview; $homePreview = $homePair[0]; $g = $homePair[1]
DrawAsset $g 'ui_home_background.png' 0 0 540 1120
DrawAsset $g 'ui_home_settings_panel.png' 24 374 493 427
$values = @('03:00','01:00','12','00:10')
for($i=0;$i -lt 4;$i++) {
    DrawCentered $g $values[$i] 34 ([System.Drawing.Color]::FromArgb(255,255,243,106)) 226 (399+$i*98) 190 70 'Consolas'
}
DrawAsset $g 'ui_home_start_button.png' 36 806 468 122
DrawAsset $g 'ui_ring_footer.png' 7 926 525 194
$g.Dispose(); $homePreview.Save((Join-Path $report 'v1.3.10-首页素材合成预览.png'),[System.Drawing.Imaging.ImageFormat]::Png)

$timerPair = NewPreview; $timer = $timerPair[0]; $g = $timerPair[1]
DrawAsset $g 'ui_timer_background.png' 0 0 540 1120
DrawAsset $g 'ui_timer_round_panel.png' 60 250 420 112
DrawCentered $g '第 1 / 12 回合' 35 ([System.Drawing.Color]::White) 90 270 360 72
DrawAsset $g 'ui_timer_panel.png' 26 355 488 302
Draw-SevenSegmentTime $g '02:48' 270 470 120
DrawAsset $g 'ui_timer_ring.png' 0 730 540 280
DrawAsset $g 'ui_boxer_back.png' 0 638 540 309
DrawAsset $g 'ui_timer_status_panel.png' 42 545 456 140
DrawAsset $g 'ui_phase_training.png' 140 580 260 70
DrawAsset $g 'ui_timer_controls_dock.png' 0 924 540 196
DrawAssetAlphaCentered $g 'ui_label_pause.png' 136 1009
DrawAssetAlphaCentered $g 'ui_label_end.png' 393 1009
$g.Dispose(); $timer.Save((Join-Path $report 'v1.3.10-训练页素材合成预览.png'),[System.Drawing.Imaging.ImageFormat]::Png)

$comparison = [System.Drawing.Bitmap]::new(1080,1120,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$cg = [System.Drawing.Graphics]::FromImage($comparison)
$cg.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$cg.DrawImage($homePreview,[System.Drawing.Rectangle]::new(0,0,540,1120))
$cg.DrawImage($timer,[System.Drawing.Rectangle]::new(540,0,540,1120))
$cg.Dispose(); $comparison.Save((Join-Path $report 'v1.3.10-双页素材合成预览.png'),[System.Drawing.Imaging.ImageFormat]::Png)
$homePreview.Dispose(); $timer.Dispose(); $comparison.Dispose()
Write-Output 'Generated v1.3.10 non-runtime material composition previews.'
