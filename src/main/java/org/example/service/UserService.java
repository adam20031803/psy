package org.example.service;

import org.example.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    // ⚠️ BASE CORRECTE
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // 🔐 LOGIN
    public User login(String email, String pwd) throws Exception {
        String sql = "SELECT * FROM user WHERE email = ? AND pwd = ?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, pwd);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        }
        return null;
    }

    // 📋 GET ALL USERS (AUCUN FILTRE)
    public List<User> getAll() throws Exception {
        List<User> list = new ArrayList<>();

        String sql = "SELECT * FROM user";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        System.out.println("UserService.getAll() → " + list.size() + " users");
        return list;
    }

    // ➕ ADD USER
    public int addUser(User u) throws Exception {
        String sql = """
            INSERT INTO user (nom, prenom, age, tel, email, pwd)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setInt(3, u.getAge());
            ps.setString(4, u.getTel());
            ps.setString(5, u.getEmail());
            ps.setString(6, u.getPwd());

            return ps.executeUpdate();
        }
    }

    // ✏️ UPDATE USER
    public boolean update(int id, String nom, String prenom, int age,
                          String tel, String email, String pwd) throws Exception {

        String sql = """
            UPDATE user
            SET nom = ?, prenom = ?, age = ?, tel = ?, email = ?, pwd = ?
            WHERE id = ?
        """;

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setInt(3, age);
            ps.setString(4, tel);
            ps.setString(5, email);
            ps.setString(6, pwd);
            ps.setInt(7, id);

            return ps.executeUpdate() > 0;
        }
    }

    // 🗑️ DELETE USER
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM user WHERE id = ?";

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // 🔄 MAPPING ResultSet → User
    private User map(ResultSet rs) throws Exception {
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
