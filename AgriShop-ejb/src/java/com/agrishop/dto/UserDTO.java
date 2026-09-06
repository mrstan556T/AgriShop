package com.agrishop.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private Integer id;
    private String userCode;
    private String username;
    private String fullName;
    private String role;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}