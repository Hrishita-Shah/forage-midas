package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.UserRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<UserRecord, Long> {
    UserRecord findById(long id);
    UserRecord findByName(String name);

    @Query("SELECT u FROM UserRecord u WHERE u.name = :name")
    UserRecord findUserByName(@Param("name") String name);
}
