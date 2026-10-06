package ivanmikhnevich.officebookings.security;

import java.util.Set;

public record CurrentUser(String userId, Set<String> roles) {
    public boolean hasAnyRole(String... candidates) {
        for(String c : candidates) {
            if (roles.contains(c)) return true;
        }
        return false;
    }
}