package io.bugsbunny.infrastructure;

import java.io.Serializable;

public class Tenant implements Serializable {
    private String principal;

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
