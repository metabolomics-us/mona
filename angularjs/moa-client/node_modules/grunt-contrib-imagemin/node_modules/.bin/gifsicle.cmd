@IF EXIST "%~dp0\node.exe" (
  "%~dp0\node.exe"  "%~dp0\..\gifsicle\cli.js" %*
) ELSE (
  node  "%~dp0\..\gifsicle\cli.js" %*
)