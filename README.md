# WSO2-OKTA-KM-Migrator
This is a client to migrate the resident OKTA KM from WSO2 APIM 3.0.0 to third party OKTA KM in WSO2 APIM 4.2.0

Once project is built you can execute the .jar using the following command. You can choose the command according to the DB implementation you are having

java -cp NN-Migration-Client-1.0-SNAPSHOT-jar-with-dependencies.jar:mysql-connector-java-8.0.27.jar MigrateService

java -cp NN-Migration-Client-1.0-SNAPSHOT-jar-with-dependencies.jar:mssql-jdbc-12.4.2.jre11.jar MigrateService
