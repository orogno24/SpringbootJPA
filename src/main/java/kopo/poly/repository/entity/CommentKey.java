package kopo.poly.repository.entity;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
@Builder
@NoArgsConstructor
@Data
public class CommentKey implements Serializable {

    private long commentSeq;
    private long noticeSeq;

    public CommentKey(long commentSeq, long noticeSeq) {
        this.commentSeq = commentSeq;
        this.noticeSeq = noticeSeq;
    }
}
