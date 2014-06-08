@IF EXIST "%~dp0\node.exe" (
  "%~dp0\node.exe"  "%~dp0\..\pngquant-bin\cli.js" %*
) ELSE (
  node  "%~dp0\..\pngquant-bin\cli.js" %*
)