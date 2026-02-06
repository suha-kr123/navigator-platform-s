package com.nivasafinance.features.person.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.person.enums.Gender;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.javers.core.metamodel.annotation.TypeName;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@TypeName("person")
@Table(name = "n_person")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Person extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "display_name", length = 300)
    private String displayName;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mobile_numbers", columnDefinition = "jsonb")
    private List<MobileNumberDetails> mobileNumbers;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address", columnDefinition = "jsonb")
    private List<AddressData> address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "identifiers", columnDefinition = "jsonb")
    private List<IdentifierData> identifiers;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_ext", columnDefinition = "jsonb")
    private Map<String, Object> extData;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cb_enquiry_id", columnDefinition = "jsonb")
    private List<Long> cbEnquiryId;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cb_details", columnDefinition = "jsonb")
    private CreditBureauDetails cbDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consent_details", columnDefinition = "jsonb")
    private List<ConsentInfo> consentDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentInfo {
        private Long id;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreditBureauDetails {
        private Long latestSuccessEnquiryId;
        private Long latestEnquiryId;
    }
}

