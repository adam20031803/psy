-- Create 'reclamation' table
USE psy;

CREATE TABLE IF NOT EXISTS reclamation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    sujet VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    priorite VARCHAR(50) DEFAULT 'NORMALE',
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
