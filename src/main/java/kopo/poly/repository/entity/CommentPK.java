package kopo.poly.repository.entity;

import lombok.Builder;
import lombok.Data;

import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Embeddable
@Builder
@NoArgsConstructor
public class CommentPK implements Serializable {

    private long commentSeq;
    private long noticeSeq;

    public CommentPK(long commentSeq, long noticeSeq) {
        this.commentSeq = commentSeq;
        this.noticeSeq = noticeSeq;
    }

}
