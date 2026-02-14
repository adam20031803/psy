package org.example.model;

public class User {

    private int id;
    private String nom;
    private String prenom;
    private int age;
    private String tel;
    private String email;
    private String pwd;
    private String role;

    // ✅ No-args constructor (recommended)
    public User() {
    }

    // ✅ Full constructor (DB read)
    public User(int id, String nom, String prenom, int age,
                String tel, String email, String pwd, String role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.tel = tel;
        this.email = email;
        this.pwd = pwd;
        this.role = role;
    }

    // ✅ Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getAge() { return age; }
    public String getTel() { return tel; }
    public String getEmail() { return email; }
    public String getPwd() { return pwd; }
    public String getRole() { return role; }

    // ✅ Setters (needed for editable TableView / future forms)
    public void setId(int id) { this.id = id; }

    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setAge(int age) { this.age = age; }
    public void setTel(String tel) { this.tel = tel; }
    public void setEmail(String email) { this.email = email; }
    public void setPwd(String pwd) { this.pwd = pwd; }

    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", age=" + age +
                ", tel='" + tel + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
