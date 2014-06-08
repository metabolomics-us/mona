@IF EXIST "%~dp0\node.exe" (
  "%~dp0\node.exe"  "%~dp0\..\svgo\bin\svgo" %*
) ELSE (
  node  "%~dp0\..\svgo\bin\svgo" %*
)