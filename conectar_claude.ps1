# Registra los dos servidores MCP en Claude Desktop sin borrar la configuracion existente.
# Claude instalado desde Microsoft Store lee la configuracion desde LocalCache del paquete.
$ErrorActionPreference = 'Stop'
$lab  = Split-Path -Parent $MyInvocation.MyCommand.Path
$java = 'C:\Program Files\Android\Android Studio\jbr\bin\java.exe'
if (-not (Test-Path $java)) { $java = 'java' }

$dirs = @(Join-Path $env:APPDATA 'Claude')
Get-ChildItem (Join-Path $env:LOCALAPPDATA 'Packages') -Directory -Filter 'Claude_*' -ErrorAction SilentlyContinue |
  ForEach-Object { $dirs += (Join-Path $_.FullName 'LocalCache\Roaming\Claude') }
# Esta version de Claude Desktop lee la configuracion desde Local\Claude-Data\claude_*
Get-ChildItem (Join-Path $env:LOCALAPPDATA 'Claude-Data') -Directory -ErrorAction SilentlyContinue |
  Where-Object { Test-Path (Join-Path $_.FullName 'claude_desktop_config.json') } |
  ForEach-Object { $dirs += $_.FullName }

$sec = Read-Host -AsSecureString 'Clave de MySQL para root (Enter si no tiene)'
$pwd = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec))

foreach ($cfgDir in $dirs) {
  $cfg = Join-Path $cfgDir 'claude_desktop_config.json'
  New-Item -ItemType Directory -Force -Path $cfgDir | Out-Null
  if (Test-Path $cfg) {
    Copy-Item $cfg "$cfg.bak" -Force
    $json = Get-Content $cfg -Raw | ConvertFrom-Json
    if ($null -eq $json) { $json = [pscustomobject]@{} }
  } else { $json = [pscustomobject]@{} }
  if (-not $json.PSObject.Properties['mcpServers']) { $json | Add-Member -NotePropertyName mcpServers -NotePropertyValue ([pscustomobject]@{}) }

  $shop = [pscustomobject]@{ command = $java; args = @('-jar', (Join-Path $lab 'mcp-server-tdea\target\mcp-server-tdea-0.0.1-SNAPSHOT.jar')) }
  $bank = [pscustomobject]@{ command = $java; args = @('-jar', (Join-Path $lab 'mcp-server-banco\target\mcp-server-banco-0.0.1-SNAPSHOT.jar'));
                             env = [pscustomobject]@{ DB_USER = 'root'; DB_PASSWORD = $pwd } }
  $json.mcpServers | Add-Member -Force -NotePropertyName 'shopping-list-server' -NotePropertyValue $shop
  $json.mcpServers | Add-Member -Force -NotePropertyName 'bank-server' -NotePropertyValue $bank

  [IO.File]::WriteAllText($cfg, ($json | ConvertTo-Json -Depth 30), (New-Object Text.UTF8Encoding $false))
  Write-Host "Guardado: $cfg"
}
Write-Host "Servidores registrados: shopping-list-server, bank-server"
