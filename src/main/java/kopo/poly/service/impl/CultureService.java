package kopo.poly.service.impl;

import kopo.poly.dto.CultureDTO;
import kopo.poly.persistance.mongodb.ICultureMapper;
import kopo.poly.service.ICultureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CultureService implements ICultureService {

    private final ICultureMapper cultureMapper; // MongoDB에 저장할 Mapper

    @Override
    public List<CultureDTO> getCultureListNearby(CultureDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getCultureListNearby Start!");

        // MongoDB에 저장된 컬렉션 이름
        String colNm = "CULTURE";

        List<CultureDTO> rList = cultureMapper.getCultureListNearby(colNm, pDTO);

        log.info(this.getClass().getName() + ".getCultureListNearby End!");

        return rList;
    }

    @Override
    public CultureDTO getCultureInfo(String nSeq) throws Exception {

        log.info(this.getClass().getName() + ".getCultureInfo Start!");

        String colNm = "CULTURE";

        CultureDTO pDTO = cultureMapper.getCultureInfo(colNm, nSeq);

        log.info(this.getClass().getName() + ".getCultureInfo End!");

        return pDTO;
    }
}
