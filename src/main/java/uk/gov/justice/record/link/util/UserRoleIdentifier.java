package uk.gov.justice.record.link.util;

import java.util.List;

public class UserRoleIdentifier {

    public static boolean isInternalAdmin(List<String> roles) {
        return roles.stream()
                .anyMatch(role -> role.toLowerCase().contains("intern"));
    }

    public static boolean isInternalViewer(List<String> roles) {
        return roles.stream()
                .anyMatch(role -> role.toLowerCase().contains("viewer"));
    }

    public static boolean isExternalUser(List<String> roles) {
        return roles.stream()
                .anyMatch(role -> role.toLowerCase().contains("extern"));
    }
}
