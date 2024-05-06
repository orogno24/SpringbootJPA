package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "NOTICE_COMMENT")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
//@Cacheable
@Data
@IdClass(CommentKey.class)
@Entity
public class CommentEntity {

    @Id
    @Column(name = "notice_seq")
    private Long noticeSeq;

    @Id
    @Column(name = "comment_seq")
    private Long commentSeq;

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
