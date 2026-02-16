package org.example.dao.motivation;

import java.util.List;

public interface CrudRecompense<T> {
    void create(T t);

    List<T> readAll();

    void update(T t);

    void delete(int id); // soft delete

    void delete_reel(int id); // delete réel
}
