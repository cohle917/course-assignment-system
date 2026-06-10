package com.teaching.user.service;

import com.teaching.common.entity.User;
import com.teaching.common.entity.User.Role;
import com.teaching.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Map<String, Object> login(String username, String password, String role) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        
        String userRoleStr = user.getRole().name().toLowerCase();
        String inputRoleStr = role.toLowerCase();
        
        if (!userRoleStr.equals(inputRoleStr)) {
            throw new RuntimeException("角色不匹配");
        }
        
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        result.put("role", user.getRole().name());
        result.put("token", "mock-token-" + user.getId());
        
        return result;
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public List<User> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole().equals(role))
                .collect(Collectors.toList());
    }
}
