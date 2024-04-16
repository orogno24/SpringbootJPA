var map; // 전역 변수로 지도 객체 선언

function initMap() {
    latitude = parseFloat(document.getElementById('latitude').textContent);
    longitude = parseFloat(document.getElementById('longitude').textContent);
    var mapContainer = document.getElementById('map'), // 지도를 표시할 div
        mapOption = {
            center: new kakao.maps.LatLng(latitude, longitude), // 지도의 중심좌표
            level: 3 // 지도의 확대 레벨
        };

    map = new kakao.maps.Map(mapContainer, mapOption); // 지도 객체를 전역 변수로 할당
    var markerPosition = new kakao.maps.LatLng(latitude, longitude);
    var marker = new kakao.maps.Marker({position: markerPosition});
    marker.setMap(map);

    // 카테고리 코드로 음식점 검색
    searchPlacesByCategory();
}

function searchPlacesByCategory() {
    var ps = new kakao.maps.services.Places(); // map 객체 없이 초기화
    var category = 'FD6'; // '음식점' 카테고리 코드
    var placesList = document.getElementById('placesList');

    ps.categorySearch(category, placesSearchCB, {
        location: new kakao.maps.LatLng(latitude, longitude),
        radius: 500 // 검색 반경 설정 (미터 단위)
    });

    function placesSearchCB(data, status, pagination) {
        if (status === kakao.maps.services.Status.OK) {
            var listStr = '';
            var count = Math.min(5, data.length);
            for (var i = 0; i < count; i++) {
                listStr += '<li><a href="' + data[i].place_url + '" target="_blank">' + data[i].place_name + '</a> - ' + data[i].address_name + '<br>' + data[i].category_name + '</li>';
            }
            placesList.innerHTML = listStr;
        } else {
            placesList.innerHTML = '검색 결과가 없습니다.';
        }
    }
}