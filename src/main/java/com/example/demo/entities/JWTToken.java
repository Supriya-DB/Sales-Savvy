package com.example.demo.entities;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "jwt_tokens")
public class JWTToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tokenId;

    @Column(nullable = false)
    private String token;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public JWTToken() {
    }

    public Integer getTokenId() {
        return tokenId;
    }

    public void setTokenId(Integer tokenId) {
        this.tokenId = tokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

	public JWTToken(Integer tokenId, String token, LocalDateTime createdAt, LocalDateTime expiresAt, User user) {
		super();
		this.tokenId = tokenId;
		this.token = token;
		this.createdAt = createdAt;
		this.expiresAt = expiresAt;
		this.user = user;
	}
	public JWTToken(User user, String token, LocalDateTime expiresAt) {
	    this.user = user;
	    this.token = token;
	    this.createdAt = LocalDateTime.now();
	    this.expiresAt = expiresAt;
	}

	public JWTToken(String token, LocalDateTime expiresAt, User user) {
		super();
		this.token = token;
		this.expiresAt = expiresAt;
		this.user = user;
	}
    
}