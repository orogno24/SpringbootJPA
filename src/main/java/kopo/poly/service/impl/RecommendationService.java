package kopo.poly.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.config.TfIdfCalculator;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.RedisDTO;
import kopo.poly.service.IRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService implements IRecommendationService {

    @Autowired
    private KoreanTextAnalysisService textAnalysisService;
    private final TfIdfCalculator tfIdfCalculator = new TfIdfCalculator();

    public List<ApiDTO> getRecommendedEvents(RedisDTO redisDTO, ApiDTO pDTO, List<String> interestKeywords) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> rContent = objectMapper.readValue(redisDTO.contents(), new TypeReference<List<Map<String, Object>>>() {});
        List<ApiDTO> allEvents = rContent.stream()
                .map(content -> objectMapper.convertValue(content, ApiDTO.class))
                .collect(Collectors.toList());

        // TF-IDF 모델 훈련
        List<List<String>> documents = allEvents.stream()
                .map(event -> textAnalysisService.analyzeText(event.title()))
                .collect(Collectors.toList());
        tfIdfCalculator.train(documents);

        Set<ApiDTO> uniqueEvents = new HashSet<>();
        for (String keyword : interestKeywords) {
            List<String> keywordNouns = textAnalysisService.analyzeText(keyword);
            List<ApiDTO> keywordEvents = allEvents.stream()
                    .filter(e -> tfIdfCalculator.calculateDocumentScore(textAnalysisService.analyzeText(e.title()), keywordNouns) > 0)
                    .sorted((e1, e2) -> Double.compare(
                            tfIdfCalculator.calculateDocumentScore(textAnalysisService.analyzeText(e2.title()), keywordNouns),
                            tfIdfCalculator.calculateDocumentScore(textAnalysisService.analyzeText(e1.title()), keywordNouns)
                    ))
                    .limit(5)
                    .collect(Collectors.toList());
            uniqueEvents.addAll(keywordEvents);
        }

        List<ApiDTO> recommendedEvents = uniqueEvents.stream()
                .sorted((e1, e2) -> Double.compare(
                        tfIdfCalculator.calculateDocumentScore(textAnalysisService.analyzeText(e2.title()), interestKeywords),
                        tfIdfCalculator.calculateDocumentScore(textAnalysisService.analyzeText(e1.title()), interestKeywords)
                ))
                .limit(3)
                .collect(Collectors.toList());

        return recommendedEvents;
    }
}
