package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "NETWORK_INFO")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
//@Cacheable
@Entity
public class NetworkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "network_seq")
    private Long networkSeq;

    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "contents", length = 1000)
    private String contents;

    @Column(name = "start_date", nullable = false)
    private String startDate;

    @Column(name = "end_date", nullable = false)
    private String endDate;

    @Column(name = "event_seq", length = 100)
    private String eventSeq;

    @Column(name = "event_name", length = 1000)
    private String eventName;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "reg_dt", updatable = false)
    private String regDt;

    @Column(name = "type", updatable = false)
    private String type;

}
