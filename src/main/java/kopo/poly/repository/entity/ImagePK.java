package kopo.poly.repository.entity;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Builder
@NoArgsConstructor
public class ImagePK implements Serializable {

    private long imageSeq;
    private long noticeSeq;

    public ImagePK(long imageSeq, long noticeSeq) {
        this.imageSeq = imageSeq;
        this.noticeSeq = noticeSeq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImagePK imagePK = (ImagePK) o;
        return imageSeq == imagePK.imageSeq && noticeSeq == imagePK.noticeSeq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageSeq, noticeSeq);
    }
}
