package kopo.poly.repository.entity;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Builder
@NoArgsConstructor
@Data
public class ImageKey implements Serializable {

    private long imageSeq;
    private long noticeSeq;

    public ImageKey(long imageSeq, long noticeSeq) {
        this.imageSeq = imageSeq;
        this.noticeSeq = noticeSeq;
    }
}
