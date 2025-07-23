package uk.gov.justice.record.link.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "ccms_user")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class CcmsUser extends BaseEntity {

    @Column(name = "login_id", nullable = false)
    private String loginId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "firm_name")
    private String firmName;

    @Column(name = "firm_code")
    private String firmCode;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "ccmsUser", cascade = CascadeType.PERSIST)
    private Set<LinkedRequest> linkedRequests;

}
