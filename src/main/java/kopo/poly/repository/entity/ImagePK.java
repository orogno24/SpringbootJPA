package kopo.poly.repository.entity;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
}
