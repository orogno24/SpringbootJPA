package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "NOTICE_IMAGE")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
//@Cacheable
@IdClass(ImagePK.class)
@Entity
public class NoticeImageEntity {

    @Id
    @Column(name = "notice_seq")
    private Long noticeSeq;

    @Id
    @Column(name = "image_seq")
    private Long imageSeq;

    @Column(name = "image_path", nullable = false)
    private String imagePath;
}
