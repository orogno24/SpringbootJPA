package kopo.poly.persistance.mongodb.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CultureMapper extends AbstractMongoDBComon implements ICultureMapper {

    private final MongoTemplate mongodb;

    /**
     * 좌표 기준 문화시설 리스트 조회
     *
     * @param colNm 조회할 컬렉션 이름
     * @param pDTO 검색할 조건 목록
     * @return 문화시설 리스트
     */
    @Override
    public List<CultureDTO> getCultureListNearby(String colNm, CultureDTO pDTO) throws Exception {
        log.info(this.getClass().getName() + "getCultureListNearby Start!");

        List<CultureDTO> rList = new LinkedList<>();

        MongoCollection<Document> col = mongodb.getCollection(colNm);

        Point refPoint = new Point(new Position(pDTO.yCoord(), pDTO.xCoord())); // 사용자 위치
        Bson geoFilter = Filters.near("location", refPoint, pDTO.radius(), 0.0); // 3km 반경 검색, 최소 거리는 0

        String selectedSubjcode = pDTO.subjcode();

        // subjcode가 지정된 경우만 필터에 추가
        Bson finalFilter = geoFilter;
        if (selectedSubjcode != null && !selectedSubjcode.isEmpty()) {
            Bson subjcodeFilter = Filters.eq("subjcode", selectedSubjcode);
            finalFilter = Filters.and(geoFilter, subjcodeFilter); // 지리적 필터와 subjcode 필터 결합
        }

        Document projection = new Document();
        projection.append("subjcode", "$subjcode")
                .append("mainImg", "$mainImg")
                .append("facName", "$facName")
                .append("addr", "$addr")
                .append("num", "$num")
                .append("xCoord", "$xCoord")
                .append("yCoord", "$yCoord");

        Document sort = new Document().append("num", 1);

        FindIterable<Document> rs = col.find(finalFilter)
                .projection(projection)
                .sort(sort);

        for (Document doc : rs) {
            String subjcode = CmmUtil.nvl(doc.getString("subjcode"));
            String mainImg = CmmUtil.nvl(doc.getString("mainImg"));
            String facName = CmmUtil.nvl(doc.getString("facName"));
            String addr = CmmUtil.nvl(doc.getString("addr"));
            Integer num = doc.getInteger("num");
            Double xCoord = doc.getDouble("xCoord");
            Double yCoord = doc.getDouble("yCoord");

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

        log.info(this.getClass().getName() + "getCultureListNearby End!");

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
        projection.append("mainImg", "$mainImg");
        projection.append("facName", "$facName");
        projection.append("addr", "$addr");
        projection.append("closeday", "$closeday");
        projection.append("phne", "$phne");
        projection.append("facDesc", "$facDesc");
        projection.append("homepage", "$homepage");
        projection.append("xCoord", "$xCoord");
        projection.append("yCoord", "$yCoord");
        projection.append("_id", 0);

        int nSeqInt = Integer.parseInt(nSeq);
        Document query = new Document("num", nSeqInt);

        Document doc = col.find(query).projection(projection).first();

        CultureDTO rDTO = null;

        String mainImg = CmmUtil.nvl(doc.getString("mainImg"));
        String facName = CmmUtil.nvl(doc.getString("facName"));
        String addr = CmmUtil.nvl(doc.getString("addr"));
        String closeday = CmmUtil.nvl(doc.getString("closeday"));
        String phne = CmmUtil.nvl(doc.getString("phne"));
        String facDesc = CmmUtil.nvl(doc.getString("facDesc"));
        String homepage = CmmUtil.nvl(doc.getString("homepage"));
        Double xCoord = doc.getDouble("xCoord");
        Double yCoord = doc.getDouble("yCoord");

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

        log.info(this.getClass().getName() + ".getCultureInfo End!");

        return rDTO;
    }
}
