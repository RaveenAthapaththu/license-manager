@REM ----------------------------------------------------------------------------
@REM  Copyright 2001-2006 The Apache Software Foundation.
@REM
@REM  Licensed under the Apache License, Version 2.0 (the "License");
@REM  you may not use this file except in compliance with the License.
@REM  You may obtain a copy of the License at
@REM
@REM       http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM  Unless required by applicable law or agreed to in writing, software
@REM  distributed under the License is distributed on an "AS IS" BASIS,
@REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM  See the License for the specific language governing permissions and
@REM  limitations under the License.
@REM ----------------------------------------------------------------------------
@REM
@REM   Copyright (c) 2001-2006 The Apache Software Foundation.  All rights
@REM   reserved.

@echo off

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto WinNTGetScriptDir

@REM The 4NT Shell from jp software
:4NTArgs
set CMD_LINE_ARGS=%$
goto WinNTGetScriptDir

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto Win9xGetScriptDir
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

:Win9xGetScriptDir
set SAVEDIR=%CD%
%0\
cd %0\..\.. 
set BASEDIR=%CD%
cd %SAVEDIR%
set SAVE_DIR=
goto repoSetup

:WinNTGetScriptDir
set BASEDIR=%~dp0\..

:repoSetup
set REPO=


if "%JAVACMD%"=="" set JAVACMD=java

if "%REPO%"=="" set REPO=%BASEDIR%\repo

set CLASSPATH="%BASEDIR%"\config;"%REPO%"\mysql\mysql-connector-java\5.1.44\mysql-connector-java-5.1.44.jar;"%REPO%"\javax\mail\mail\1.4.7\mail-1.4.7.jar;"%REPO%"\javax\activation\activation\1.1.1\activation-1.1.1.jar;"%REPO%"\com\google\api-client\google-api-client\1.23.0\google-api-client-1.23.0.jar;"%REPO%"\com\google\oauth-client\google-oauth-client\1.23.0\google-oauth-client-1.23.0.jar;"%REPO%"\com\google\http-client\google-http-client\1.23.0\google-http-client-1.23.0.jar;"%REPO%"\org\apache\httpcomponents\httpclient\4.0.1\httpclient-4.0.1.jar;"%REPO%"\org\apache\httpcomponents\httpcore\4.0.1\httpcore-4.0.1.jar;"%REPO%"\commons-logging\commons-logging\1.1.1\commons-logging-1.1.1.jar;"%REPO%"\commons-codec\commons-codec\1.3\commons-codec-1.3.jar;"%REPO%"\com\google\code\findbugs\jsr305\1.3.9\jsr305-1.3.9.jar;"%REPO%"\com\google\http-client\google-http-client-jackson2\1.23.0\google-http-client-jackson2-1.23.0.jar;"%REPO%"\com\fasterxml\jackson\core\jackson-core\2.1.3\jackson-core-2.1.3.jar;"%REPO%"\com\google\guava\guava-jdk5\17.0\guava-jdk5-17.0.jar;"%REPO%"\com\google\oauth-client\google-oauth-client-jetty\1.23.0\google-oauth-client-jetty-1.23.0.jar;"%REPO%"\com\google\oauth-client\google-oauth-client-java6\1.23.0\google-oauth-client-java6-1.23.0.jar;"%REPO%"\org\mortbay\jetty\jetty\6.1.26\jetty-6.1.26.jar;"%REPO%"\org\mortbay\jetty\jetty-util\6.1.26\jetty-util-6.1.26.jar;"%REPO%"\org\mortbay\jetty\servlet-api\2.5-20081211\servlet-api-2.5-20081211.jar;"%REPO%"\com\google\apis\google-api-services-gmail\v1-rev73-1.23.0\google-api-services-gmail-v1-rev73-1.23.0.jar;"%REPO%"\org\apache\torque\village\3.3.1\village-3.3.1.jar;"%REPO%"\com\google\code\gson\gson\2.8.2\gson-2.8.2.jar;"%REPO%"\com\googlecode\json-simple\json-simple\1.1.1\json-simple-1.1.1.jar;"%REPO%"\com\esotericsoftware\yamlbeans\yamlbeans\1.06\yamlbeans-1.06.jar;"%REPO%"\org\json\json\20180130\json-20180130.jar;"%REPO%"\org\jdom\jdom\2.0.2\jdom-2.0.2.jar;"%REPO%"\org\wso2\internalapps\licencemanager-thirdpartylibrary\1.0.0\licencemanager-thirdpartylibrary-1.0.0.jar

set ENDORSED_DIR=
if NOT "%ENDORSED_DIR%" == "" set CLASSPATH="%BASEDIR%"\%ENDORSED_DIR%\*;%CLASSPATH%

if NOT "%CLASSPATH_PREFIX%" == "" set CLASSPATH=%CLASSPATH_PREFIX%;%CLASSPATH%

@REM Reaching here means variables are defined and arguments have been captured
:endInit

%JAVACMD% %JAVA_OPTS%  -classpath %CLASSPATH% -Dapp.name="LM_DB_CRON_JOB" -Dapp.repo="%REPO%" -Dapp.home="%BASEDIR%" -Dbasedir="%BASEDIR%" org.wso2.internalapps.lm.thirdpartylibrary.lmdbcronjobs.mainclass.CronJobMain %CMD_LINE_ARGS%
if %ERRORLEVEL% NEQ 0 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=%ERRORLEVEL%

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set CMD_LINE_ARGS=
goto postExec

:endNT
@REM If error code is set to 1 then the endlocal was done already in :error.
if %ERROR_CODE% EQU 0 @endlocal


:postExec

if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
