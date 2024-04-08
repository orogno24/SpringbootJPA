package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "CULTURE_EVENT")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
@Cacheable
@Entity
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincrement와 같음
    @Column(name = "event_seq")
    private Long eventSeq;

    @NonNull
    @Column(name = "event_sort", nullable = false)
    private String eventSort;

    @NonNull
    @Column(name = "event_place", nullable = false)
    private String eventPlace;

    @NonNull
    @Column(name = "event_name", nullable = false)
    private String eventName;

    @NonNull
    @Column(name = "event_where", nullable = false)
    private String eventWhere;

    @NonNull
    @Column(name = "event_who", nullable = false)
    private String eventWho;

    @NonNull
    @Column(name = "event_money", nullable = false)
    private String eventMoney;

    @NonNull
    @Column(name = "event_whoinfo", nullable = false)
    private String eventWhoinfo;

    @NonNull
    @Column(name = "event_info", nullable = false)
    private String eventInfo;

    @NonNull
    @Column(name = "event_etc", nullable = false)
    private String eventEtc;

    @NonNull
    @Column(name = "event_pageinfo", nullable = false)
    private String eventPafeInfo;

    @NonNull
    @Column(name = "event_image", nullable = false)
    private String eventImage;

    @NonNull
    @Column(name = "event_startdate", nullable = false)
    private String eventStartdate;

    @NonNull
    @Column(name = "event_enddate", nullable = false)
    private String eventEnddate;

    @NonNull
    @Column(name = "event_theme", nullable = false)
    private String eventTheme;

    @NonNull
    @Column(name = "event_x", nullable = false)
    private String eventX;

    @NonNull
    @Column(name = "event_y", nullable = false)
    private String eventY;

    @NonNull
    @Column(name = "event_free", nullable = false)
    private String eventFree;
}
