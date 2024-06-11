package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "NETWORK_INFO")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
@Cacheable
@Entity
public class NetworkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "network_seq")
    private int networkSeq;

    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "contents", length = 1000)
    private String contents;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants;

    @Column(name = "event_seq", length = 100)
    private String eventSeq;

    @Column(name = "culture_seq", length = 100)
    private String cultureSeq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserInfoEntity userInfo;
}
