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
    public List<CultureDTO> getCultureList() throws Exception {

        log.info(this.getClass().getName() + ".getCultureList Start!");

        // MongoDB에 저장된 컬렉션 이름
        String colNm = "CULTURE";

        List<CultureDTO> rList = cultureMapper.getCultureList(colNm);

        log.info(this.getClass().getName() + ".getCultureList End!");

        return rList;
    }
}
