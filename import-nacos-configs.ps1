[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$nacosUrl = "http://127.0.0.1:8848"
$username = "nacos"
$password = "nacos"

$configDir = "nacos-configs"
$services = @("user-service", "course-service", "homework-service")

Write-Host "`n=== Importing configs to Nacos ===" -ForegroundColor Cyan

foreach ($service in $services) {
    $configFile = Join-Path $configDir "$service.yml"
    if (Test-Path $configFile) {
        try {
            $content = Get-Content $configFile -Raw -Encoding UTF8
            
            $url = "$nacosUrl/nacos/v1/cs/configs"
            
            $params = @{
                dataId = "$service.yml"
                group = "DEFAULT_GROUP"
                content = $content
                type = "yaml"
            }
            
            $securePassword = ConvertTo-SecureString $password -AsPlainText -Force
            $credential = New-Object System.Management.Automation.PSCredential ($username, $securePassword)
            
            $response = Invoke-RestMethod -Uri $url -Method POST -Body $params -Credential $credential
            
            Write-Host "OK: $service.yml" -ForegroundColor Green
        }
        catch {
            Write-Host "ERROR: $service.yml - $_" -ForegroundColor Red
        }
    }
    else {
        Write-Host "WARN: Config file not found: $configFile" -ForegroundColor Yellow
    }
}

Write-Host "`n=== Import completed ===" -ForegroundColor Cyan
Write-Host "Nacos Console: $nacosUrl/nacos"
Read-Host "Press Enter to exit"