package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kopo.poly.controller.response.CommonResponse;
import kopo.poly.dto.CultureDTO;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.WeatherDTO;
import kopo.poly.service.ICultureService;
import kopo.poly.service.impl.NaverSearchService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequestMapping(value = "/culture")
@RequiredArgsConstructor
@Controller
public class CultureController {

    private final ICultureService cultureService;
    private final NaverSearchService naverSearchService;

    @Value("${culture.api.key}")
    private String apiKey;

    @GetMapping("/mongoTest")
    public String mongoTest() throws Exception {
        cultureService.getCultureApi(apiKey);
        return "culture/mongoTest";
    }

    @PostMapping(value="basic")
    public ResponseEntity basic(@Valid @RequestBody CultureDTO pDTO, BindingResult bindingResult) throws Exception {
        log.info(this.getClass().getName() + ".basic Start!");

        if(bindingResult.hasErrors()) {
            return CommonResponse.getErrors(bindingResult);
        }

        String msg = "";

        log.info("pDTO : " + pDTO);

        int res = cultureService.mongoTest(pDTO);

        if (res == 1) {
            msg = "성공";
        } else {
            msg = "실패";
        }

        MsgDTO dto = MsgDTO.builder().result(res).msg(msg).build();

        log.info(this.getClass().getName() + ".basic End!");

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto));

    }


    /**
     * 문화시설 검색 페이지
     */
    @GetMapping("cultureMap")
    public String cultureMap(HttpSession session) {
        log.info(this.getClass().getName() + ".cultureMap 함수 실행");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.length() > 0) {
            return "culture/cultureMap";
        } else {
            return "redirect:/user/login";
        }

    }

    /**
     * 일정에 문화시설 추가
     */
    @GetMapping("selectCulture")
    public String selectCulture(HttpSession session) {
        log.info(this.getClass().getName() + ".selectCulture 함수 실행");

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

        if (userId.length() > 0) {
            return "culture/selectCulture";
        } else {
            return "redirect:/user/login";
        }
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

        if (userId.length() > 0) {

            log.info("nSeq: " + nSeq);
            log.info("userId: " + userId);

            CultureDTO rDTO = Optional.ofNullable(cultureService.getCultureInfo(nSeq))
                    .orElseGet(() -> CultureDTO.builder().build());

            // 조회된 결과값 넣어주기
            model.addAttribute("rDTO", rDTO);

        }  else {

            return "redirect:/user/login";

        }

        log.info(this.getClass().getName() + ".cultureInfo End!");

        return "culture/cultureInfo";
    }

    /**
     * 해당 문화시설의 네이버 블로그 검색 결과 조회
     */
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