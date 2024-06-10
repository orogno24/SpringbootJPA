package kopo.poly.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Table(name = "CHAT_INFO")
@DynamicInsert // 값이 NULL이 아닌것만 INSERT함
@DynamicUpdate // 값이 NULL이 아닌것만 UPDATE함
@Builder
@Cacheable
@Entity
public class ChatEntitiy implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincrement와 같음
    @Column(name = "chat_seq")
    private Long ChatSeq;

    @NonNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "room_name")
    private String roomName;

}
