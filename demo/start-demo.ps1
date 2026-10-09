# Starts CPUTrade for the presentation, using the clean DEMO database
# (cputrade_demo). Your normal `cputrade` database is not touched.
# Run from the project root:  .\demo\start-demo.ps1
# Opens two windows (backend, frontend). Wait ~20s, then open http://localhost:5173
$root = Split-Path -Parent $PSScriptRoot
$env:DB_URL = 'jdbc:mysql://localhost:3306/cputrade_demo?useSSL=false&serverTimezone=Africa/Johannesburg'
Start-Process powershell -ArgumentList '-NoExit','-Command',"cd '$root\backend'; `$env:DB_URL='$($env:DB_URL)'; .\mvnw.cmd spring-boot:run"
Start-Process powershell -ArgumentList '-NoExit','-Command',"cd '$root\frontend'; npm run dev"
