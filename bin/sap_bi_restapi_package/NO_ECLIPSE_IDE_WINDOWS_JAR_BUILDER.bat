


"C:\Program Files\java\jdk1.8.0_144\bin\javac.exe" -d . -classpath json-simple.jar  *.java

copy *.gif sap_bi_restapi_package

"C:\Program Files\java\jdk1.8.0_144\bin\jar.exe" cmf  sap_bi_restapi.mf  sap_bi_restapi.jar  *.java *.gif  *.pdf  *.mf  NO_ECLIPSE_IDE_WINDOWS_JAR_BUILDER.bat  json-simple.jar  README.txt sap_bi_restapi_package

