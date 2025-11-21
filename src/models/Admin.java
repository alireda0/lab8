/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

public class Admin extends User {

    public static final String ROLE_ADMIN = "ADMIN";

    public Admin(int userId, String username, String email, String password, String role, boolean isAlreadyHashed) {
        super(userId, username, email, password, role, isAlreadyHashed);
    }
    
}

