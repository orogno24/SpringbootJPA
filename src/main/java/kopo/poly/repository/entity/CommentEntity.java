package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "COMMENT")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
//@Cacheable
@Entity
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincrement와 같음
    @Column(name = "comment_seq")
    private Long commentSeq;

    @NonNull
    @Column(name = "notice_seq", length = 500, nullable = false)
    private Long noticeSeq;

    @NonNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NonNull
    @Column(name = "contents", nullable = false)
    private String contents;

    @Column(name = "reg_dt", updatable = false)
    private String regDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserInfoEntity userInfo;
}
