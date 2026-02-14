package org.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";       // ton user MySQL
    private static final String PASSWORD = "";       // ton mot de passe MySQL

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie à la base psy !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
