package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.CultureDTO;
import kopo.poly.service.ICultureService;
import kopo.poly.util.CmmUtil;
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

    @GetMapping("cultureList")
    public String cultureList() {
        log.info(this.getClass().getName() + ".cultureList 함수 실행");
        return "culture/cultureList";
    }

    @GetMapping("cultureMap")
    public String cultureMap() {
        log.info(this.getClass().getName() + ".cultureMap 함수 실행");
        return "culture/cultureMap";
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

    /**
     * 좌표 기준 문화시설 리스트 가져오기
     */
    @ResponseBody
    @GetMapping(value = "getCultureListNearby")
    public List<CultureDTO> getCultureListNearby(HttpServletRequest request) throws Exception {

        log.info(this.getClass().getName() + ".getCultureListNearby Start!");

        double longitude = Double.parseDouble(CmmUtil.nvl(request.getParameter("longitude"))); // 위도
        double latitude = Double.parseDouble(CmmUtil.nvl(request.getParameter("latitude"))); // 경도
        double radius = Double.parseDouble(CmmUtil.nvl(request.getParameter("radius"))); // 경도
        String subjcode = CmmUtil.nvl(request.getParameter("subjcode")); // 분류

        CultureDTO pDTO = CultureDTO.builder().xCoord(latitude).yCoord(longitude).radius(radius).subjcode(subjcode).build();

        log.info("longitude : " + pDTO.yCoord() + " latitude : " + pDTO.xCoord() + " radius : " + pDTO.radius() + " subjcode : " + pDTO.subjcode());

        List<CultureDTO> rList = Optional.ofNullable(cultureService.getCultureListNearby(pDTO))
                .orElseGet(ArrayList::new);

        log.info(this.getClass().getName() + ".getCultureListNearby End!");

        return rList;
    }
}