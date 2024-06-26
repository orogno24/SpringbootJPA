package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "USER_INFO")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
//@Cacheable
@Entity
public class UserInfoEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @NonNull
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "password")
    private String password;

    @NonNull
    @Column(name = "email",  nullable = false)
    private String email;

    @Column(name = "reg_id", nullable = false)
    private String regId;

    @Column(name = "reg_dt", nullable = false)
    private String regDt;

    @Column(name = "chg_id", nullable = false)
    private String chgId;

    @Column(name = "chg_dt", nullable = false)
    private String chgDt;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "provider")
    private String provider;

//    @Column(name = "provider_id")
//    private String providerId;
}
