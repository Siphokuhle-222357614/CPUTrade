# Starts CPUTrade for the presentation. The app now uses the normal `cputrade`
# database, which holds the demo data. Run from the project root:  .\demo\start-demo.ps1
# Opens two windows (backend, frontend). Wait ~20s, then open http://localhost:5173
$root = Split-Path -Parent $PSScriptRoot
Start-Process powershell -ArgumentList '-NoExit','-Command',"cd '$root\backend'; .\mvnw.cmd spring-boot:run"
Start-Process powershell -ArgumentList '-NoExit','-Command',"cd '$root\frontend'; npm run dev"
