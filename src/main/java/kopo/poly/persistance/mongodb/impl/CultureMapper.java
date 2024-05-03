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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CultureMapper extends AbstractMongoDBComon implements ICultureMapper {

    private final MongoTemplate mongodb;

    @Override
    public List<CultureDTO> getCultureList(String colNm) throws Exception {

        log.info(this.getClass().getName() + "getCultureList Start!");

        // 조회 결과를 전달하기 위한 객체 생성하기
        List<CultureDTO> rList = new LinkedList<>();

        MongoCollection<Document> col = mongodb.getCollection(colNm);

        // 조회 결과 중 출력할 컬럼들(SQL의 SELECT절과 FROM절 가운데 컬럼들과 유사함)
        Document projection = new Document();
        projection.append("num", "$num");
        projection.append("facName", "$facName");
        projection.append("phne", "$phne");
        projection.append("xCoord", "$xCoord");
        projection.append("yCoord", "$yCoord");

        // MongoDB는 무조건 ObjectId가 자동생성되며, ObjectId는 사용하지 않을때, 조회할 필요가 없음
        // ObjectId를 가지고 오지 않을 때 사용함
        projection.append("_id", 0);

        // MongoDB의 find 명령어를 통해 조회할 경우 사용함
        // 조회하는 데이터의 양이 적은 경우, find를 사용하고, 데이터양이 많은 경우 무조건 Aggregate 사용한다.

        FindIterable<Document> rs = col.find(new Document()).projection(projection);

        for (Document doc : rs) {
            Integer num = doc.getInteger("num");
            String facName = CmmUtil.nvl(doc.getString("facName"));
            String phne = CmmUtil.nvl(doc.getString("phne"));
            Double xCoord = doc.getDouble("xCoord");
            Double yCoord = doc.getDouble("yCoord");

            CultureDTO rDTO = CultureDTO.builder().num(num).facName(facName).phne(phne).xCoord(xCoord).yCoord(yCoord).build();

            rList.add(rDTO);
        }

        log.info(this.getClass().getName() + "getCultureList End!");

        return rList;
    }
}
