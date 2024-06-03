package kopo.poly.repository.entity;

import lombok.Builder;
import lombok.Data;

import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentPK commentPK = (CommentPK) o;
        return commentSeq == commentPK.commentSeq && noticeSeq == commentPK.noticeSeq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentSeq, noticeSeq);
    }

}
