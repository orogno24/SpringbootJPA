package kopo.poly.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.CultureDTO;
import kopo.poly.persistance.mongodb.ICultureMapper;
import kopo.poly.service.ICultureFeign;
import kopo.poly.service.ICultureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CultureService implements ICultureService {

    private final ICultureMapper cultureMapper; // MongoDB에 저장할 Mapper
    private final ICultureFeign cultureFeign;

    @Value("${culture.api.key}")
    private String apiKey;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void scheduledCultureDataUpdate() {
        try {
            int result = getCultureApi(apiKey);
            log.info("문화시설 데이터 업데이트 성공: {}", result);
        } catch (Exception e) {
            log.error("문화시설 데이터 업데이트 실패", e);
        }
    }

    @Override
    public int getCultureApi(String apikey) throws Exception {
        List<Map<String, Object>> rContent = new ArrayList<>();
        String colNm = "CULTURE_DATA";
        int startIndex = 1;
        int pageSize = 1000; // 각 페이지에서 가져올 데이터 수
        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            String response = cultureFeign.getCultureApi(apikey, "json", startIndex, startIndex + pageSize - 1);
            Map<String, Object> rMap = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});

            // culturalSpaceInfo가 존재하는지 확인
            Map<String, Object> culturalSpaceInfo = (Map<String, Object>) rMap.get("culturalSpaceInfo");

            if (culturalSpaceInfo == null) {
                break;
            }

            List<Map<String, Object>> rows = (List<Map<String, Object>>) culturalSpaceInfo.get("row");

            // 더 이상 행이 없으면 루프 종료
            if (rows == null || rows.isEmpty()) {
                break;
            }

            rContent.addAll(rows);

            // 다음 배치를 위한 시작 인덱스 증가
            startIndex += pageSize;
        }

        // Map<String, Object>를 CultureDTO로 변환
        List<CultureDTO> pList = new ArrayList<>();
        for (Map<String, Object> map : rContent) {
            CultureDTO cultureDTO = objectMapper.convertValue(map, CultureDTO.class);
            pList.add(cultureDTO);
        }

        int res = cultureMapper.cultureDataInsert(pList, colNm);

        return res;
    }

    /**
     * 좌표 기준 문화시설 리스트 가져오기
     */
    @Override
    public List<CultureDTO> getCultureListNearby(CultureDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getCultureListNearby Start!");

        // MongoDB에 저장된 컬렉션 이름
        String colNm = "CULTURE_DATA";

        List<CultureDTO> rList = cultureMapper.getCultureListNearby(colNm, pDTO);

        log.info(this.getClass().getName() + ".getCultureListNearby End!");

        return rList;
    }

    /**
     * 문화시설 상세정보 조회
     */
    @Override
    public CultureDTO getCultureInfo(String nSeq) throws Exception {

        log.info(this.getClass().getName() + ".getCultureInfo Start!");

        String colNm = "CULTURE_DATA";

        CultureDTO pDTO = cultureMapper.getCultureInfo(colNm, nSeq);

        log.info(this.getClass().getName() + ".getCultureInfo End!");

        return pDTO;
    }

}
