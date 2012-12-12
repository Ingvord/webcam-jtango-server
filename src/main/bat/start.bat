@echo OFF

IF NOT DEFINED JRE32_HOME (
Echo JRE32_HOME variable is not set!
EXIT /B -1
)
IF NOT DEFINED TANGO_HOST (
Echo TANGO_HOST variable is not set!
EXIT /B -1
)
IF [%1]==[] (
Echo Tango instance name is not set! This should be passed as cli argument, i.e. "start.bat development"
EXIT /B -1
)

Echo Using JRE32_HOME=%JRE32_HOME%
Echo Using TANGO_HOST=%TANGO_HOST%
set WEBCAM_HOME=%~dp0..
Echo Using WEBCAM_HOME=%WEBCAM_HOME%
set INSTANCE_NAME=%1
Echo Using instance name=%INSTANCE_NAME%

"%JRE32_HOME%\bin\java" -Xmx1G -Djava.library.path="%WEBCAM_HOME%\lib\native\win32" -Dfile.encoding=UTF-8 -cp "%WEBCAM_HOME%\lib\WebCamServer-1.0.jar;%WEBCAM_HOME%\lib\JTangoServer-0.0.29-all.jar;%WEBCAM_HOME%\lib\jmf.jar" hzg.wpn.tango.camera.webcam.WebCam %INSTANCE_NAME% -v4