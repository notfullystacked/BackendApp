# deploy-vm.ps1
# Laegger hele Kino-systemet paa EN virtuel maskine (VM) i Azure:
#   Ubuntu-VM  ->  Docker  ->  docker compose (MySQL + backend + frontend)
#   Siden ligger bagefter paa  http://<VM'ens offentlige IP>
#
# Koer fra BackendApp-mappen i PowerShell:
#   powershell -ExecutionPolicy Bypass -File .\deploy-vm.ps1 -Name kino-vm
#   (test en branch foer den er merget:  ... -Branch vm-deployment)
#
# Slet det hele igen (stopper forbruget af kredit):
#   az group delete -n <Name>-rg --yes

param(
    [Parameter(Mandatory = $true)][string]$Name,   # fx "kino-vm"
    [string]$Location = "swedencentral",
    [string]$Size = "Standard_B2s",                # 2 vCPU, 4 GB RAM - nok til MySQL + Spring Boot + nginx
    [string]$Branch = "main"                       # hvilken branch af BackendApp der skal koeres
)

$ErrorActionPreference = "Continue"
$rg = "$Name-rg"

function Check($what) {
    if ($LASTEXITCODE -ne 0) { throw "FEJL ved: $what (se beskeden ovenfor)" }
}
function Step($text) { Write-Host "`n=== $text ===" -ForegroundColor Cyan }

Step "Tjekker Azure-login"
az account show --query name -o tsv 2>$null
if ($LASTEXITCODE -ne 0) { az login; Check "az login" }

# cloud-init: koerer automatisk paa VM'en ved foerste opstart
$cloudInit = @"
#cloud-config
package_update: true
packages:
  - git
  - curl
runcmd:
  - [ sh, -c, "curl -fsSL https://get.docker.com | sh" ]
  - [ sh, -c, "usermod -aG docker azureuser" ]
  - [ sh, -c, "mkdir -p /opt/kino && cd /opt/kino && git clone -b $Branch https://github.com/notfullystacked/BackendApp.git && git clone https://github.com/notfullystacked/FrontendApp.git" ]
  - [ sh, -c, "cd /opt/kino/BackendApp && FRONTEND_PORT=80 docker compose --profile app up -d --build" ]
"@
$cloudInitFile = Join-Path $env:TEMP "kino-cloud-init.yaml"
Set-Content -Path $cloudInitFile -Value $cloudInit -Encoding ASCII

Step "Opretter resource group ($Location)"
az group create -n $rg -l $Location -o none; Check "group create"

Step "Opretter Ubuntu-VM '$Name' ($Size) - tager et par minutter"
az vm create -g $rg -n $Name --image Ubuntu2404 --size $Size `
    --admin-username azureuser --generate-ssh-keys `
    --custom-data $cloudInitFile --public-ip-sku Standard -o none
Check "vm create"
Remove-Item $cloudInitFile -Force

Step "Aabner port 80 (hjemmesiden). Port 22 (SSH) er aaben fra start. MySQL og 8080 forbliver lukket."
az vm open-port -g $rg -n $Name --port 80 --priority 900 -o none; Check "open-port 80"

$ip = az vm show -d -g $rg -n $Name --query publicIps -o tsv; Check "hent IP"
Write-Host "VM'ens IP: $ip"

Step "Venter paa at VM'en installerer Docker og bygger containerne (typisk 5-10 minutter)"
Write-Host "Vil du foelge med? Aabn et nyt vindue og koer:"
Write-Host "  ssh azureuser@$ip `"sudo tail -f /var/log/cloud-init-output.log`""
$ok = $false
for ($i = 1; $i -le 60 -and -not $ok; $i++) {
    Start-Sleep -Seconds 20
    try {
        $r = Invoke-WebRequest -Uri "http://$ip/api/movies" -UseBasicParsing -TimeoutSec 10
        if ($r.StatusCode -eq 200) { $ok = $true }
    } catch { Write-Host "  ... ikke klar endnu ($($i * 20) sek)" }
}

Step "Faerdig"
if ($ok) {
    Write-Host "Hjemmesiden koerer:  http://$ip" -ForegroundColor Green
    Write-Host "API (eksempel):      http://$ip/api/movies" -ForegroundColor Green
} else {
    Write-Host "Siden svarer ikke endnu. Tjek loggen:  ssh azureuser@$ip `"sudo tail -n 80 /var/log/cloud-init-output.log`"" -ForegroundColor Yellow
}
Write-Host "Husk at slette det hele naar I er godkendt:  az group delete -n $rg --yes"
