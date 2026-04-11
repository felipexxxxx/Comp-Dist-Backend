param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$FrontendUrl = "http://localhost:4173"
)

$ErrorActionPreference = "Stop"

function Invoke-JsonRequest {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )

    $request = @{
        Method      = $Method
        Uri         = $Url
        Headers     = $Headers
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $request.Body = ($Body | ConvertTo-Json -Depth 6)
    }

    return Invoke-RestMethod @request
}

function Invoke-StatusRequest {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{}
    )

    try {
        Invoke-WebRequest -Method $Method -Uri $Url -Headers $Headers -UseBasicParsing | Out-Null
        return 200
    } catch {
        if ($_.Exception.Response) {
            return [int]$_.Exception.Response.StatusCode
        }

        throw
    }
}

function Wait-ForUrl {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 180
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        try {
            Invoke-WebRequest -Uri $Url -Method Get -UseBasicParsing | Out-Null
            return
        } catch {
            Start-Sleep -Seconds 2
        }
    }

    throw "Timed out waiting for $Url"
}

$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$newUserEmail = "smoke.$timestamp@healthsys.local"
$newPatientName = "Paciente Smoke $timestamp"

Write-Host "Waiting for gateway and frontend..."
Wait-ForUrl -Url "$BaseUrl/actuator/health"
Wait-ForUrl -Url $FrontendUrl

$frontendResponse = Invoke-WebRequest -Uri $FrontendUrl -Method Get -UseBasicParsing
if ($frontendResponse.StatusCode -ne 200) {
    throw "Frontend did not return HTTP 200."
}

Write-Host "Logging in with bootstrap admin..."
$login = Invoke-JsonRequest -Method Post -Url "$BaseUrl/api/auth/login" -Body @{
    email    = "admin@healthsys.local"
    password = "Admin@123"
}

if (-not $login.token) {
    throw "Login did not return a JWT token."
}

$authHeaders = @{
    Authorization = "$($login.tokenType) $($login.token)"
}

Write-Host "Validating /api/auth/me ..."
$me = Invoke-JsonRequest -Method Get -Url "$BaseUrl/api/auth/me" -Headers $authHeaders
if ($me.email -ne "admin@healthsys.local") {
    throw "Authenticated user does not match bootstrap admin."
}

Write-Host "Creating user..."
$createdUser = Invoke-JsonRequest -Method Post -Url "$BaseUrl/api/users" -Headers $authHeaders -Body @{
    name     = "Usuario Smoke $timestamp"
    email    = $newUserEmail
    password = "Smoke@12345"
    role     = "RECEPTIONIST"
}

if ($createdUser.email -ne $newUserEmail) {
    throw "User creation failed."
}

Write-Host "Listing users..."
$users = Invoke-JsonRequest -Method Get -Url "$BaseUrl/api/users" -Headers $authHeaders
if (-not ($users | Where-Object { $_.email -eq $newUserEmail })) {
    throw "Created user was not returned by /api/users."
}

Write-Host "Creating patient..."
$createdPatient = Invoke-JsonRequest -Method Post -Url "$BaseUrl/api/patients" -Headers $authHeaders -Body @{
    name      = $newPatientName
    birthDate = "1991-02-03"
    sex       = "FEMALE"
    phone     = "85999990000"
}

if (-not $createdPatient.id) {
    throw "Patient creation failed."
}

Write-Host "Listing patients..."
$patients = Invoke-JsonRequest -Method Get -Url "$BaseUrl/api/patients" -Headers $authHeaders
if (-not ($patients | Where-Object { $_.id -eq $createdPatient.id })) {
    throw "Created patient was not returned by /api/patients."
}

Write-Host "Updating patient and inactivating record..."
$updatedPatient = Invoke-JsonRequest -Method Put -Url "$BaseUrl/api/patients/$($createdPatient.id)" -Headers $authHeaders -Body @{
    name      = "$newPatientName Atualizado"
    birthDate = "1991-02-03"
    sex       = "FEMALE"
    phone     = "85888887777"
    active    = $false
}

if ($updatedPatient.active -ne $false) {
    throw "Patient update did not persist inactivation."
}

Write-Host "Revoking JWT through logout..."
$logout = Invoke-JsonRequest -Method Post -Url "$BaseUrl/api/auth/logout" -Headers $authHeaders
if ($logout.message -ne "Logout completed and token revoked.") {
    throw "Logout did not report token revocation."
}

$authStatusAfterLogout = Invoke-StatusRequest -Method Get -Url "$BaseUrl/api/auth/me" -Headers $authHeaders
if ($authStatusAfterLogout -ne 401) {
    throw "Revoked token still accesses /api/auth/me (status $authStatusAfterLogout)."
}

Write-Host "Waiting for logout propagation to patient-service..."
$revokedPatientStatus = 200
for ($attempt = 0; $attempt -lt 10; $attempt++) {
    Start-Sleep -Seconds 2
    $revokedPatientStatus = Invoke-StatusRequest -Method Get -Url "$BaseUrl/api/patients" -Headers $authHeaders
    if ($revokedPatientStatus -eq 401) {
        break
    }
}

if ($revokedPatientStatus -ne 401) {
    throw "Revoked token still accesses /api/patients after propagation window (status $revokedPatientStatus)."
}

Write-Host "Smoke test completed successfully."
