package com.example.gym.daos;

import com.example.gym.entities.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl
        extends BaseDaoImpl<UserEntity>
        implements UserDao {

    @Override
    protected Class<UserEntity> getEntityClass() {
        return UserEntity.class;
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {

        String jpql =
                "SELECT u FROM UserEntity u WHERE u.username=:username";

        List<UserEntity> users =
                entityManager.createQuery(jpql, UserEntity.class)
                        .setParameter("username", username)
                        .getResultList();

        return users.stream().findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}
