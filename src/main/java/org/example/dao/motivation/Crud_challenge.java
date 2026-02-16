package org.example.dao.motivation;

import java.util.List;

public interface Crud_challenge<T> {

    // CREATE
    void create(T t);

    // READ
    List<T> readAll();

    // UPDATE
    void update(T t);

    // DELETE
    void delete(int id);

    void delete_reel(int id);
}
