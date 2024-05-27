package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.ApiDTO;
import kopo.poly.dto.BookmarkDTO;
import kopo.poly.dto.CultureDTO;
import kopo.poly.service.ICultureService;
import kopo.poly.service.INaverSearchService;
import kopo.poly.service.impl.NaverSearchService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping(value = "/culture")
@RequiredArgsConstructor
@Controller
public class CultureController {

    private final ICultureService cultureService;
    @Autowired
    private NaverSearchService naverSearchService;

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

    /**
     * 문화시설 상세보기
     */
    @GetMapping(value = "cultureInfo")
    public String apiInfo(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".cultureInfo Start!");

        // 고유 식별자를 받는 방식에 따라 변경 필요
        String nSeq = CmmUtil.nvl(request.getParameter("nSeq"), "");
        String userId = (String) session.getAttribute("SS_USER_ID");

        log.info("nSeq: " + nSeq);
        log.info("userId: " + userId);

        CultureDTO rDTO = Optional.ofNullable(cultureService.getCultureInfo(nSeq))
                .orElseGet(() -> CultureDTO.builder().build());

        // 조회된 결과값 넣어주기
        model.addAttribute("rDTO", rDTO);

        log.info(this.getClass().getName() + ".cultureInfo End!");

        return "culture/cultureInfo";
    }

    @GetMapping("/facilities/{name}")
    public ResponseEntity<String> getFacilityDetails(@PathVariable String name) {
        try {
            String response = naverSearchService.searchBlogs(name);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving blog data: " + e.getMessage());
        }
    }

}