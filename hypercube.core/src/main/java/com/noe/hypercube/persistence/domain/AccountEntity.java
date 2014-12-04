package com.noe.hypercube.persistence.domain;

import javax.persistence.*;

@Entity
public class AccountEntity implements IEntity<Integer> {

    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String accountName;
    private Boolean attached;
    private String refreshToken;
    private String accessToken;

    @Override
    public Integer getId() {
        return id;
    }

    public AccountEntity() {
    }

    public AccountEntity(final String accountName) {
        this.accountName = accountName;
    }

    public String getAccountName() {
        return accountName;
    }

    public Boolean isAttached() {
        return attached;
    }

    public void setAttached(final Boolean attached) {
        this.attached = attached;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(final String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }
}
