package ru.practicum.shareit.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exeption.DuplicatedDataException;
import ru.practicum.shareit.exeption.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository {
    private Long index = 0L;
    private final Collection<User> users;

    @Override
    public Collection<User> findAll() {
        return users;
    }

    @Override
    public User create(User user) {
        user.setId(++index);
        users.add(user);
        return user;
    }

    @Override
    public User update(User newUser) {
        User user = users.stream()
                .filter(user1 -> user1.getId().equals(newUser.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User (id = " + newUser.getId() + ") not found!"));
        if (newUser.getEmail() != null
                && !newUser.getEmail().equals(user.getEmail())
                && isUserEmailExist(newUser.getEmail())) {
            throw new DuplicatedDataException("E-Mail " + newUser.getEmail() + " is exist!");
        }
        if (newUser.getEmail() != null
                && !newUser.getEmail().isBlank()) {
            user.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null
                && !newUser.getName().isBlank()) {
            user.setName(newUser.getName());
        }
        return user;
    }

    @Override
    public User findUserById(Long userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User (id = " + userId + ") not found!"));
    }

    @Override
    public boolean isUserEmailExist(String email) {
        return users.stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public boolean isUserExist(Long userId) {
        return users.stream()
                .anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public void delete(Long userId) {
        User user = users.stream()
                .filter(user1 -> user1.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User (id = " + userId + ") not found!"));
        users.remove(user);
    }
}
