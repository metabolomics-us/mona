@IF EXIST "%~dp0\node.exe" (
  "%~dp0\node.exe"  "%~dp0\..\optipng-bin\cli.js" %*
) ELSE (
  node  "%~dp0\..\optipng-bin\cli.js" %*
)