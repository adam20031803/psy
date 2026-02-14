package org.example.service;

import java.sql.*;
import java.time.LocalDateTime;

public class PasswordResetService {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/psy?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private static final String EMAIL_WHERE = "TRIM(LOWER(email)) = TRIM(LOWER(?))";

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM user WHERE " + EMAIL_WHERE;
        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createAndSendCode(String email) {
        if (email == null || email.trim().isEmpty()) throw new RuntimeException("Email vide.");
        email = email.trim();

        if (!emailExists(email)) throw new RuntimeException("Email introuvable.");

        String code = OtpUtil.generateCode6();
        String codeHash = OtpUtil.sha256(code);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        String sql = "UPDATE user SET reset_code_hash=?, reset_expires_at=?, reset_attempts=0 WHERE " + EMAIL_WHERE;

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codeHash);
            ps.setTimestamp(2, Timestamp.valueOf(expiresAt));
            ps.setString(3, email);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("Aucun utilisateur trouvé pour cet email.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        new SendGridMailService().sendOtp(email, code);
    }

    public boolean verifyCode(String email, String code) {
        if (email == null || email.trim().isEmpty()) return false;
        if (code == null || code.trim().isEmpty()) return false;

        email = email.trim();
        code = code.trim();

        String sql = "SELECT reset_code_hash, reset_expires_at, reset_attempts FROM user WHERE " + EMAIL_WHERE;

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;

                String dbHash = rs.getString("reset_code_hash");
                Timestamp expiresAt = rs.getTimestamp("reset_expires_at");
                int attempts = rs.getInt("reset_attempts");

                if (dbHash == null || expiresAt == null) return false;
                if (expiresAt.before(new Timestamp(System.currentTimeMillis()))) return false;
                if (attempts >= 5) return false;

                String inputHash = OtpUtil.sha256(code);

                if (!inputHash.equals(dbHash)) {
                    try (PreparedStatement ups = cn.prepareStatement(
                            "UPDATE user SET reset_attempts=reset_attempts+1 WHERE " + EMAIL_WHERE)) {
                        ups.setString(1, email);
                        ups.executeUpdate();
                    }
                    return false;
                }

                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePasswordAndClearReset(String email, String newPasswordPlain) {
        if (email == null || email.trim().isEmpty()) throw new RuntimeException("Email vide.");
        if (newPasswordPlain == null || newPasswordPlain.length() < 6)
            throw new RuntimeException("Mot de passe trop court (min 6).");

        email = email.trim();

        String newPwdHash = OtpUtil.sha256(newPasswordPlain);

        String sql = "UPDATE user SET pwd=?, reset_code_hash=NULL, reset_expires_at=NULL, reset_attempts=0 WHERE " + EMAIL_WHERE;

        try (Connection cn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, newPwdHash);
            ps.setString(2, email);

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("Aucun utilisateur trouvé pour cet email.");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
