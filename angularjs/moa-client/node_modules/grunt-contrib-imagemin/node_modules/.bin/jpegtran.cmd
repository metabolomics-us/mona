@IF EXIST "%~dp0\node.exe" (
  "%~dp0\node.exe"  "%~dp0\..\jpegtran-bin\cli.js" %*
) ELSE (
  node  "%~dp0\..\jpegtran-bin\cli.js" %*
)