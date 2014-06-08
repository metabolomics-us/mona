@IF EXIST "%~dp0\node.exe" (
  "%~dp0\node.exe"  "%~dp0\..\executable\cli.js" %*
) ELSE (
  node  "%~dp0\..\executable\cli.js" %*
)