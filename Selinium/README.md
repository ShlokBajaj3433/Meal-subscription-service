# Selinium - TestNG suite

This folder contains a standalone TestNG Selenium suite used by CI and local execution.

## Run full suite locally

Prerequisites:
- Meal Subscription application running at http://localhost:9090 (or set app.base.url)
- Java and Maven available (project wrapper is supported)

PowerShell:

```powershell
Set-Location "C:\codes\Selenium\Meal subscription service"
.\mvnw.cmd -f Selinium\pom.xml test -Dapp.base.url=http://localhost:9090 -Dbrowser=chrome -Dheadless=true
```

## Optional runtime properties

- app.base.url: target application URL, default http://localhost:9090
- browser: chrome or edge, default chrome
- headless: true or false, default true

## Suite definition

The suite file is in Selinium/testng.xml and includes:
- SampleTest
- LoginTest
- RegisterTest
- MealsTest
- DashboardTest
- AdminMealsTest
