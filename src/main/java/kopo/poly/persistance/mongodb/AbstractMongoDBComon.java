package kopo.poly.persistance.mongodb;

import com.mongodb.client.model.Indexes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMongoDBComon {

    /**
     * 인덱스 컬럼이 여러 개일때 컬렉션 생성
     *
     * @param mongodb 접속된 MongoDB
     * @param colNm   생성할 컬렉션명
     * @param index   생성할 인덱스
     * @return 생성결과
     */
    protected boolean createCollection(MongoTemplate mongodb, String colNm, String[] index) {
        log.info(this.getClass().getName() + ".createCollection Start!");

        boolean res = false;

        // 기존에 등록된 컬렉션 이름이 존재하는지 체크하고, 컬렉션이 없는 경우 생성함
        if (!mongodb.collectionExists(colNm)) {
            // 컬렉션 생성
            mongodb.createCollection(colNm);
            res = true;
        }

        // 인덱스가 존재한다면 생성
        if (index.length > 0) {
            // 'num'에 대한 오름차순 인덱스 생성
            mongodb.getCollection(colNm).createIndex(Indexes.ascending(index[0]));

            // 'location'에 대한 2dsphere 인덱스 생성
            mongodb.getCollection(colNm).createIndex(Indexes.geo2dsphere("location"));
        }

        log.info(this.getClass().getName() + ".createCollection End!");
        return res;
    }

    /**
         * 인덱스 컬림이 한 개일때 컬렉션 생성
         *
         * @param mongodb 접속된 MongoDB
         * @param colNm 생성할 컬렉션명
         * @param index 생성할 인덱스
         * @return 생성결과
         */
        protected boolean createCollection(MongoTemplate mongodb, String colNm, String index) {

            String[] indexArr = {index, "location"};

            return createCollection(mongodb, colNm, indexArr);
        }

        /**
         * 컬렉션 삭제
         *
         * @param mongodb 접속된 MongoDB
         * @param colNm 생성할 컬렉션명
         * @return 삭제결과
         */
        protected boolean dropCollection(MongoTemplate mongodb, String colNm) {

            boolean res = false;

            if (mongodb.collectionExists(colNm)) {
                mongodb.dropCollection(colNm);
                res = true;

            }

            return res;
        }


}
