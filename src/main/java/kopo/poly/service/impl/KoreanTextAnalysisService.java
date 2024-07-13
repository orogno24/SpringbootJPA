package kopo.poly.service.impl;

import kopo.poly.service.IKoreanTextAnalysisService;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KoreanTextAnalysisService implements IKoreanTextAnalysisService {

    private Komoran komoran;

    public KoreanTextAnalysisService() {
        this.komoran = new Komoran(DEFAULT_MODEL.FULL);
    }

    public List<String> analyzeText(String text) {
        KomoranResult analyzeResultList = komoran.analyze(text);
        return analyzeResultList.getNouns();  // 명사만 추출
    }
}
