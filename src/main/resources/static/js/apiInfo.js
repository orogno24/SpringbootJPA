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

    // 맵 로딩 후 첫 번째 탭 활성화 및 검색 트리거
    var firstTab = document.querySelector('.tab'); // 첫 번째 탭 요소 선택
    searchPlacesByCategory('FD6', null, firstTab); // 첫 번째 탭을 매개변수로 전달
}

function searchPlacesByCategory(category, event, tabElement) {
    // 모든 탭의 활성 상태 제거
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.remove('tab-active');
    });

    // 클릭된 탭 활성화 또는 첫 번째 탭 활성화
    if (event) {
        event.target.classList.add('tab-active');
    } else if (tabElement) {
        tabElement.classList.add('tab-active');
    }

    var ps = new kakao.maps.services.Places();
    var placesList = document.getElementById('placesList');

    ps.categorySearch(category, function (data, status, pagination) {
        if (status === kakao.maps.services.Status.OK) {
            var listStr = '';
            var count = Math.min(5, data.length);
            for (var i = 0; i < count; i++) {
                listStr += '<li class="places-list-item">' +
                    '<div class="place-info">' + // Flex 컨테이너
                    '<div class="place-name">' + data[i].place_name + '</div>' +
                    '<span class="place-detail-link" onclick="window.open(\'' + data[i].place_url + '\', \'_blank\')">상세보기</span>' + // 상세보기 링크
                    '</div>' +
                    '<span class="place-address">' + data[i].address_name + '</span><br>' +
                    '<span class="place-category">' + data[i].category_name + '</span>' +
                    '</li>';
            }
            placesList.innerHTML = listStr;
        } else {
            placesList.innerHTML = '검색 결과가 없습니다.';
        }
    }, {
        location: new kakao.maps.LatLng(latitude, longitude),
        radius: 1200 // 검색 반경 설정 (미터 단위)
    });
}

