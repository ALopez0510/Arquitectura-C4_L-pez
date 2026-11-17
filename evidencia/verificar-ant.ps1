# Verifica la disponibilidad de la consulta de citaciones de la ANT y registra el resultado.
# Uso:  powershell -ExecutionPolicy Bypass -File .\evidencia\verificar-ant.ps1 [-Cedula 1710034065]
param([string]$Cedula = "1710034065")

$url = "https://consultaweb.ant.gob.ec/PortalWEB/paginas/clientes/clp_grid_citaciones.jsp?ps_tipo_identificacion=CED&ps_identificacion=$Cedula&ps_placa="
$registro = Join-Path $PSScriptRoot "registro-ant.csv"
$fecha = Get-Date -Format "yyyy-MM-dd HH:mm:ss zzz"

try {
    $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 15 -ErrorAction Stop
    $codigo = [int]$r.StatusCode
    $detalle = "OK"
} catch {
    if ($_.Exception.Response) {
        $codigo = [int]$_.Exception.Response.StatusCode
        $detalle = $_.Exception.Response.StatusDescription
    } else {
        $codigo = 0
        $detalle = $_.Exception.Message
    }
}

if (-not (Test-Path $registro)) { "fecha,url,codigo,detalle" | Out-File $registro -Encoding utf8 }
"$fecha,""$url"",$codigo,""$detalle""" | Out-File $registro -Append -Encoding utf8

Write-Host "Fecha:  $fecha"
Write-Host "URL:    $url"
Write-Host "Código: $codigo $detalle"
if ($codigo -eq 200) {
    Write-Host "La ANT respondió. Inicie ms-ant con ANT_MODO=real." -ForegroundColor Green
} else {
    Write-Host "La ANT sigue sin responder. Mantenga ANT_MODO=simulado." -ForegroundColor Yellow
}
