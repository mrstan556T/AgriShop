package com.agrishop.service;

import com.agrishop.dto.UserDTO;
import jakarta.ejb.Local;

@Local
public interface UserServiceLocal {
    UserDTO login(String username, String password);
}