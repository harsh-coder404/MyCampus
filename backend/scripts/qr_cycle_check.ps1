# TEST FILE: Backend QR attendance verification script (manual test utility)
# This script is not used by Android/Backend runtime; run it only for QA checks.

$ErrorActionPreference = 'Stop'

function To-JsonBody($obj) {
    return ($obj | ConvertTo-Json)
}

function Login-User($email, $password, $role) {
    $body = To-JsonBody @{ email = $email; password = $password; role = $role }
    return Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/auth/login' -ContentType 'application/json' -Body $body
}

$report = @()

$prof = Login-User 'proff@abc.com' 'Proff@12' 'PROFESSOR'
$stud = Login-User 'harsh_@abc.com' 'Harsh_A@12' 'STUDENT'
$pToken = $prof.data.accessToken
$sToken = $stud.data.accessToken

$courses = Invoke-RestMethod -Method Get -Uri 'http://127.0.0.1:8081/attendance/professor/courses' -Headers @{ Authorization = ('Bearer ' + $pToken) }
$courseId = [long]$courses.data[0].id

$start = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/attendance/sessions/start' -ContentType 'application/json' -Headers @{ Authorization = ('Bearer ' + $pToken) } -Body (To-JsonBody @{ courseId = $courseId; ttlSeconds = 60 })
$sessionId = $start.data.sessionId
$timestamp = [int64]$start.data.timestamp
$expiresAt = [int64]$start.data.expiresAtEpochSec
$report += [PSCustomObject]@{
    Step = 'Start'
    Expected = 'SUCCESS + sessionId'
    Actual = ($start.status + ' / ' + $sessionId)
    Result = $(if ($start.status -eq 'SUCCESS' -and $sessionId) { 'PASS' } else { 'FAIL' })
}

$markBody = @{ studentId = 'ignored'; courseId = $courseId; sessionId = $sessionId; timestamp = $timestamp; deviceId = 'emu-test-1' }
$mark = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/attendance/mark' -ContentType 'application/json' -Headers @{ Authorization = ('Bearer ' + $sToken) } -Body (To-JsonBody $markBody)
$report += [PSCustomObject]@{
    Step = 'Mark'
    Expected = 'SUCCESS'
    Actual = $mark.status
    Result = $(if ($mark.status -eq 'SUCCESS') { 'PASS' } else { 'FAIL' })
}

$dupCode = 200
try {
    Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/attendance/mark' -ContentType 'application/json' -Headers @{ Authorization = ('Bearer ' + $sToken) } -Body (To-JsonBody $markBody) | Out-Null
} catch {
    $dupCode = $_.Exception.Response.StatusCode.value__
}
$report += [PSCustomObject]@{
    Step = 'Duplicate Mark Block'
    Expected = 'HTTP 409'
    Actual = ('HTTP ' + $dupCode)
    Result = $(if ($dupCode -eq 409) { 'PASS' } else { 'FAIL' })
}

$waitSec = [Math]::Max(0, [int]($expiresAt - [DateTimeOffset]::UtcNow.ToUnixTimeSeconds() + 1))
Start-Sleep -Seconds $waitSec

$final = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/attendance/sessions/finalize' -ContentType 'application/json' -Headers @{ Authorization = ('Bearer ' + $pToken) } -Body (To-JsonBody @{ courseId = $courseId; sessionId = $sessionId })
$report += [PSCustomObject]@{
    Step = 'Finalize'
    Expected = 'SUCCESS + totals'
    Actual = ($final.status + ' / present=' + $final.data.presentCount + ', total=' + $final.data.totalStudents)
    Result = $(if ($final.status -eq 'SUCCESS' -and [int]$final.data.totalStudents -ge 1) { 'PASS' } else { 'FAIL' })
}

$overall = 'PASS'
foreach ($r in $report) {
    if ($r.Result -eq 'FAIL') { $overall = 'FAIL' }
}

$lines = @()
$lines += ('OVERALL: ' + $overall)
$lines += ''
$lines += ($report | Format-Table -AutoSize | Out-String)
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$lines | Set-Content (Join-Path $scriptDir 'qr_cycle_report.txt')


