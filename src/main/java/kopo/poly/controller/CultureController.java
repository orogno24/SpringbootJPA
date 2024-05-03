package kopo.poly.controller;

import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.CultureDTO;
import kopo.poly.service.ICultureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping(value = "/culture/v1")
@RequiredArgsConstructor
@Controller
public class CultureController {

    private final ICultureService cultureService;

    @GetMapping("cultureTest")
    public String cultureTest() {
        log.info(this.getClass().getName() + ".cultureTest 함수 실행");
        return "culture/cultureTest";
    }

    /**
     * 문화시설 리스트 가져오기
     */
    @ResponseBody
    @PostMapping(value = "getCultureList")
    public ResponseEntity getCultureList() throws Exception {

        log.info(this.getClass().getName() + ".getCultureList Start!");

        List<CultureDTO> rList = Optional.ofNullable(cultureService.getCultureList())
                        .orElseGet(ArrayList::new);

        log.info(this.getClass().getName() + ".getCultureList End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList));
    }
}