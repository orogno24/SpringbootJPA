package kopo.poly.persistance.mongodb.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import kopo.poly.dto.CultureDTO;
import kopo.poly.persistance.mongodb.AbstractMongoDBComon;
import kopo.poly.persistance.mongodb.ICultureMapper;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CultureMapper extends AbstractMongoDBComon implements ICultureMapper {

    private final MongoTemplate mongodb;

    @Override
    public int cultureDataInsert(List<CultureDTO> pList, String colNm) {

        log.info(this.getClass().getName() + ".cultureDataInsert Start!");

        int res = 0;

        // 기존 데이터 삭제
        super.dropCollection(mongodb, colNm);

        // 컬렉션 및 인덱스 생성
        super.createCollection(mongodb, colNm, "num");

        MongoCollection<Document> col = mongodb.getCollection(colNm);

        for (CultureDTO pDTO : pList) {
            Map<String, Object> location = new HashMap<>();
            location.put("coordinates", Arrays.asList(pDTO.yCoord(), pDTO.xCoord()));
            location.put("type", "Point");

            // CultureDTO를 Map으로 변환
            Map<String, Object> documentMap = new ObjectMapper().convertValue(pDTO, Map.class);
            // location 필드를 Map에 추가
            documentMap.put("location", location);

            col.insertOne(new Document(documentMap));
        }

        res = 1;

        log.info(this.getClass().getName() + ".cultureDataInsert End!");

        return res;
    }

    /**
     * 좌표 기준 문화시설 리스트 조회
     *
     * @param colNm 조회할 컬렉션 이름
     * @param pDTO 검색할 조건 목록
     * @return 문화시설 리스트
     */
    @Override
    public List<CultureDTO> getCultureListNearby(String colNm, CultureDTO pDTO) throws Exception {
        log.info(this.getClass().getName() + " getCultureListNearby Start!");

        List<CultureDTO> rList = new LinkedList<>();

        MongoCollection<Document> col = mongodb.getCollection(colNm);

        Point refPoint = new Point(new Position(pDTO.yCoord(), pDTO.xCoord())); // 사용자 위치
        Bson geoFilter = Filters.near("location", refPoint, pDTO.radius(), 0.0); // 지정된 반경 내 검색

        String selectedSubjcode = pDTO.subjcode();

        // subjcode가 지정된 경우만 필터에 추가
        Bson finalFilter = geoFilter;
        if (selectedSubjcode != null && !selectedSubjcode.isEmpty()) {
            Bson subjcodeFilter = Filters.eq("SUBJCODE", selectedSubjcode); // 필드 이름을 맞추기
            finalFilter = Filters.and(geoFilter, subjcodeFilter); // 지리적 필터와 subjcode 필터 결합
        }

        Document projection = new Document();
        projection.append("SUBJCODE", "$SUBJCODE")
                .append("MAIN_IMG", "$MAIN_IMG")
                .append("FAC_NAME", "$FAC_NAME")
                .append("ADDR", "$ADDR")
                .append("NUM", "$NUM")
                .append("X_COORD", "$X_COORD")
                .append("Y_COORD", "$Y_COORD");

        Document sort = new Document().append("NUM", 1);

        FindIterable<Document> rs = col.find(finalFilter)
                .projection(projection)
                .sort(sort);

        for (Document doc : rs) {
            String subjcode = CmmUtil.nvl(doc.getString("SUBJCODE"));
            String mainImg = CmmUtil.nvl(doc.getString("MAIN_IMG"));
            String facName = CmmUtil.nvl(doc.getString("FAC_NAME"));
            String addr = CmmUtil.nvl(doc.getString("ADDR"));
            Integer num = doc.getInteger("NUM");
            Double xCoord = doc.getDouble("X_COORD");
            Double yCoord = doc.getDouble("Y_COORD");

            // 모든 필드를 CultureDTO에 맞춰 설정
            CultureDTO rDTO = CultureDTO.builder()
                    .subjcode(subjcode)
                    .mainImg(mainImg)
                    .facName(facName)
                    .addr(addr)
                    .num(num)
                    .xCoord(xCoord)
                    .yCoord(yCoord)
                    .build();

            rList.add(rDTO);
        }

        log.info("rList : " + rList);

        log.info(this.getClass().getName() + " getCultureListNearby End!");

        return rList;
    }

    /**
     * 문화시설 상세정보 조회
     *
     * @param colNm 조회할 컬렉션 이름
     * @return 문화시설 리스트
     */
    @Override
    public CultureDTO getCultureInfo(String colNm, String nSeq) throws Exception {

        log.info(this.getClass().getName() + ".getCultureInfo Start!");

        MongoCollection<Document> col = mongodb.getCollection(colNm);

        Document projection = new Document();
        projection.append("MAIN_IMG", "$MAIN_IMG");
        projection.append("FAC_NAME", "$FAC_NAME");
        projection.append("ADDR", "$ADDR");
        projection.append("CLOSEDAY", "$CLOSEDAY");
        projection.append("PHNE", "$PHNE");
        projection.append("FAC_DESC", "$FAC_DESC");
        projection.append("HOMEPAGE", "$HOMEPAGE");
        projection.append("X_COORD", "$X_COORD");
        projection.append("Y_COORD", "$Y_COORD");
        projection.append("_id", 0); // 기본 ID 필드는 제외

        int nSeqInt = Integer.parseInt(nSeq);
        Document query = new Document("NUM", nSeqInt); // 필드 이름 수정

        Document doc = col.find(query).projection(projection).first();

        CultureDTO rDTO = null;

        if (doc != null) { // doc이 null인 경우를 처리
            String mainImg = CmmUtil.nvl(doc.getString("MAIN_IMG"));
            String facName = CmmUtil.nvl(doc.getString("FAC_NAME"));
            String addr = CmmUtil.nvl(doc.getString("ADDR"));
            String closeday = CmmUtil.nvl(doc.getString("CLOSEDAY"));
            String phne = CmmUtil.nvl(doc.getString("PHNE"));
            String facDesc = CmmUtil.nvl(doc.getString("FAC_DESC"));
            String homepage = CmmUtil.nvl(doc.getString("HOMEPAGE"));
            Double xCoord = doc.getDouble("X_COORD");
            Double yCoord = doc.getDouble("Y_COORD");

            rDTO = CultureDTO.builder()
                    .mainImg(mainImg)
                    .facName(facName)
                    .addr(addr)
                    .closeday(closeday)
                    .phne(phne)
                    .facDesc(facDesc)
                    .homepage(homepage)
                    .xCoord(xCoord)
                    .yCoord(yCoord)
                    .build();
        }

        log.info(this.getClass().getName() + ".getCultureInfo End!");

        return rDTO;
    }

}
