$ErrorActionPreference='Stop'
function Login-User($email,$password,$role){
  $body = @{email=$email; password=$password; role=$role} | ConvertTo-Json
  Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/auth/login' -ContentType 'application/json' -Body $body
}
$report = New-Object System.Collections.Generic.List[object]
$prof = Login-User 'proff@abc.com' 'Proff@12' 'PROFESSOR'
$stud = Login-User 'harsh_@abc.com' 'Harsh_A@12' 'STUDENT'
$pToken = $prof.data.accessToken
$sToken = $stud.data.accessToken
$courses = Invoke-RestMethod -Method Get -Uri 'http://127.0.0.1:8081/attendance/professor/courses' -Headers @{ Authorization = ('Bearer ' + $pToken) }
$courseId = [long]$courses.data[0].id
$startBody = @{ courseId = $courseId; ttlSeconds = 60 } | ConvertTo-Json
$start = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/attendance/sessions/start' -ContentType 'application/json' -Headers @{ Authorization = ('Bearer ' + $pToken) } -Body $startBody
$sessionId = $start.data.sessionId
$timestamp = [int64]$start.data.timestamp
$expiresAt = [int64]$start.data.expiresAtEpochSec
$startResult = 'FAIL'; if ($start.status -eq 'SUCCESS' -and $sessionId) { $startResult = 'PASS' }
$report.Add([PSCustomObject]@{ Step='Start'; Expected='SUCCESS + sessionId'; Actual=($start.status + ' / ' + $sessionId); Result=$startResult })
$markBody = @{ studentId = 'ignored'; courseId = $courseId; sessionId = $sessionId; timestamp = $timestamp; deviceId = 'emu-test-1' } | ConvertTo-Json
$mark = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/attendance/mark' -ContentType 'application/json' -Headers @{ Authorization = ('Bearer ' + $sToken) } -Body $markBody
$markResult = 'FAIL'; if ($mark.status -eq 'SUCCESS') { $markResult = 'PASS' }
$report.Add([PSCustomObject]@{ Step='Mark'; Expected='SUCCESS'; Actual=$mark.status; Result=$markResult })
$dupCode = 200
try { Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/attendance/mark' -ContentType 'application/json' -Headers @{ Authorization = ('Bearer ' + $sToken) } -Body $markBody | Out-Null } catch { $dupCode = $_.Exception.Response.StatusCode.value__ }
$dupResult = 'FAIL'; if ($dupCode -eq 409) { $dupResult = 'PASS' }
$report.Add([PSCustomObject]@{ Step='Duplicate Mark Block'; Expected='HTTP 409'; Actual=('HTTP ' + $dupCode); Result=$dupResult })
$now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$waitSec = [Math]::Max(0, [int]($expiresAt - $now + 1))
Start-Sleep -Seconds $waitSec
$finalBody = @{ courseId = $courseId; sessionId = $sessionId } | ConvertTo-Json
$final = Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8081/attendance/sessions/finalize' -ContentType 'application/json' -Headers @{ Authorization = ('Bearer ' + $pToken) } -Body $finalBody
$finalResult = 'FAIL'; if ($final.status -eq 'SUCCESS' -and [int]$final.data.totalStudents -ge 1) { $finalResult = 'PASS' }
$finalActual = $final.status + ' / present=' + $final.data.presentCount + ', total=' + $final.data.totalStudents
$report.Add([PSCustomObject]@{ Step='Finalize'; Expected='SUCCESS + totals'; Actual=$finalActual; Result=$finalResult })
$overall = 'PASS'; foreach($r in $report){ if($r.Result -eq 'FAIL'){ $overall = 'FAIL' } }
$result = [PSCustomObject]@{ overall=$overall; steps=$report }
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$result | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $scriptDir 'qr_cycle_report.json')

