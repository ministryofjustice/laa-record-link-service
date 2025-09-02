package uk.gov.justice.record.link.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "link_request", indexes = {
    @Index(name = "linked_request_idam_first_name", columnList = "idam_first_name"),
    @Index(name = "linked_request_idam_last_name", columnList = "idam_last_name"),
    @Index(name = "linked_request_created_date", columnList = "created_date"),
    @Index(name = "linked_request_decision_date", columnList = "decision_date"),
    @Index(name = "linked_request_laa_assignee", columnList = "laa_assignee"),
    @Index(name = "linked_request_status", columnList = "status"),
    @Index(name = "linked_request_provided_login_id", columnList = "provided_old_login_id")
})
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Getter
public final class LinkedRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ccms_user_id", foreignKey = @ForeignKey(name = "FK_link_request_ccms_user_id"))
    private CcmsUser ccmsUser;

    @Column(name = "provided_old_login_id", nullable = false)
    private String oldLoginId;

    @Column(name = "idam_legacy_user_id", nullable = false, unique = true)
    private String idamLegacyUserId;

    @Column(name = "idam_first_name", nullable = false)
    private String idamFirstName;

    @Column(name = "idam_last_name", nullable = false)
    private String idamLastName;

    @Column(name = "idam_firm_name")
    private String idamFirmName;

    @Column(name = "idam_firm_code")
    private String idamFirmCode;

    @Column(name = "idam_email", nullable = false, unique = true)
    private String idamEmail;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(name = "decision_reason")
    private String decisionReason;

    @Column(name = "laa_assignee")
    private String laaAssignee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "additional_info")
    private String additionalInfo;
}
