package uk.gov.justice.record.link.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleIdentifierTest {

    @Test
    void detectsInternalAdmin_whenRoleContainsIntern_caseInsensitive() {
        assertThat(UserRoleIdentifier.isInternalAdmin(List.of("LAA-Intern-Admin"))).isTrue();
        assertThat(UserRoleIdentifier.isInternalAdmin(List.of("some-INTERN-role"))).isTrue();
        assertThat(UserRoleIdentifier.isInternalAdmin(List.of("viewer", "EXTERNAL"))).isFalse();
        assertThat(UserRoleIdentifier.isInternalAdmin(List.of())).isFalse();
    }

    @Test
    void detectsInternalViewer_whenRoleContainsViewer_caseInsensitive() {
        assertThat(UserRoleIdentifier.isInternalViewer(List.of("some-Viewer-role"))).isTrue();
        assertThat(UserRoleIdentifier.isInternalViewer(List.of("VIEWER"))).isTrue();
        assertThat(UserRoleIdentifier.isInternalViewer(List.of("admin", "external", "intern"))).isFalse();
        assertThat(UserRoleIdentifier.isInternalViewer(List.of())).isFalse();
    }

    @Test
    void detectsExternalUser_whenRoleContainsExtern_caseInsensitive() {
        assertThat(UserRoleIdentifier.isExternalUser(List.of("External-User"))).isTrue();
        assertThat(UserRoleIdentifier.isExternalUser(List.of("some-EXTERN-role"))).isTrue();
        assertThat(UserRoleIdentifier.isExternalUser(List.of("admin", "viewer"))).isFalse();
        assertThat(UserRoleIdentifier.isExternalUser(List.of())).isFalse();
    }
}
