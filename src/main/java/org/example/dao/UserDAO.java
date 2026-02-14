package org.example.dao;

import org.example.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public List<User> findAll() throws Exception {
        List<User> list = new ArrayList<>();

        String sql = "SELECT * FROM user";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getInt("age"),
                        rs.getString("tel"),
                        rs.getString("email"),
                        rs.getString("pwd"),
                        rs.getString("role")
                ));
            }
        }
        return list;
    }

    public User findByEmail(String email) throws Exception {
        String sql = "SELECT * FROM user WHERE email = ?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getInt("age"),
                        rs.getString("tel"),
                        rs.getString("email"),
                        rs.getString("pwd"),
                        rs.getString("role")
                );
            }
        }
        return null;
    }
}
