package uk.gov.justice.record.link.service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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

    @Column(name = "email")
    private String email;




}
