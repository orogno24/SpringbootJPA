package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "SCHEDULE_BOOKMARK")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
@Cacheable
@Entity
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincrement와 같음
    @Column(name = "schedule_seq")
    private Long scheduleSeq;

    @NonNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "reg_dt", updatable = false)
    private String regDt;

    @NonNull
    @Column(name = "event_seq", nullable = false)
    private String eventSeq;

}