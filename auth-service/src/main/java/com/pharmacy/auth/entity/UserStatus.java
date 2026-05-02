package com.pharmacy.auth.entity;

public enum UserStatus {
    PENDING_VERIFICATION,  // registered but email not yet verified
    ACTIVE,
    INACTIVE,
    SUSPENDED  // suspended accounts can't log in
}
