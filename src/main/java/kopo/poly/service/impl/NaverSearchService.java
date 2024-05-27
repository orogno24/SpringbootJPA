package kopo.poly.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class NaverSearchService {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    public String searchBlogs(String query) {
        String text;
        try {
            text = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패", e);
        }

        String apiURL = "https://openapi.naver.com/v1/search/blog?query=" + text;

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        String responseBody = get(apiURL, requestHeaders);

        return parseResponse(responseBody);
    }

    private String get(String apiUrl, Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readBody(con.getInputStream());
            } else {
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private HttpURLConnection connect(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private String readBody(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }

    private String parseResponse(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode items = root.path("items");

            StringBuilder result = new StringBuilder();
            result.append("<ul class=\"blog-results\">");

            int count = 0;
            for (JsonNode item : items) {
                if (count >= 5) break;

                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy년 M월 d일");

                Date postDate = inputDateFormat.parse(item.path("postdate").asText());
                String formattedDate = outputDateFormat.format(postDate);

                result.append("<li class=\"blog-item\">")
                        .append("<div class=\"blog-content\">")
                        .append("<a href=\"")
                        .append(item.path("link").asText())
                        .append("\" class=\"blog-link\">") // blog-item에 하이퍼링크 추가
                        .append("<h3 class=\"blog-title\">")
                        .append(item.path("title").asText().replaceAll("<[^>]*>", ""))
                        .append("</h3>")
                        .append("</p>")
                        .append("<p class=\"blog-description\">")
                        .append(item.path("description").asText().replaceAll("<[^>]*>", ""))
                        .append("</p>")
                        .append("<p class=\"blog-metadata\">")
                        .append("<img src=\"/assets/img/naver.png\" class=\"naver-icon\" />")
                        .append("작성자: ")
                        .append("<span class=\"blogger-name\">")
                        .append(item.path("bloggername").asText())
                        .append("</span>")
                        .append(" | 작성일: ")
                        .append("<span class=\"post-date\">")
                        .append(formattedDate) // 형식 변경된 날짜 사용
                        .append("</span>")
                        .append("</a>")
                        .append("</div>")
                        .append("</li>");

                count++;
            }

            result.append("</ul>");
            return result.toString();
        } catch (IOException | ParseException e) {
            throw new RuntimeException("API 응답 파싱 실패", e);
        }
    }


}
