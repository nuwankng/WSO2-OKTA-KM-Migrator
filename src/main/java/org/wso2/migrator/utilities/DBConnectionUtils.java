package org.wso2.migrator.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class DBConnectionUtils {
//    private static String dbhost = "jdbc:mysql://localhost:3306/420_apimdb";

    private static String dbhost = "jdbc:sqlserver://localhost:1433;databaseName=WSO2AM_DB";
//    private static String dbhost = "jdbc:mysql://localhost:3306/420_apimdb";
    private static String username = "sa";
//    private static String username = "root";
    private static String password = "Test@92nuwan";
//    private static String password = "root@1234";
    private static Connection conn;

    @SuppressWarnings("finally")
    public static Connection createNewDBconnection() {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Provide the information related to the deployment or environment you are performing the " +
                "post migration actions.\n  ** If you need the keep the default value you can just press Enter Key **");
        System.out.println("Please provide your database host : (Default = " + dbhost + ")");
        String inputString = scanner.nextLine();
        if (!inputString.isEmpty()){
            dbhost = inputString;
        }
        System.out.println("DB Host is registered as : " + dbhost);

        System.out.println("Please provide your database username : (Default = " + username + ")");
        inputString = scanner.nextLine();
        if (!inputString.isEmpty()){
            username = inputString;
        }
        System.out.println("DB user is registered as : " + username);

        System.out.println("Please provide your database password : (Default = " + password + ")");
        inputString = scanner.nextLine();
        if (!inputString.isEmpty()){
            password = inputString;
        }
        System.out.println("DB Password is registered as : " + password);

        try {
            conn = DriverManager.getConnection(
                    dbhost, username, password);
        } catch (SQLException e) {
            System.out.println("Error : Cannot create database connection");
            e.printStackTrace();
        } finally {
            return conn;
        }
    }
}