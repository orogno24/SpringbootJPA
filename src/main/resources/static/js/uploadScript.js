function uploadImage() {
    var fileInput = document.getElementById('profileImage');
    var file = fileInput.files[0];

    // 파일이 선택되지 않았을 경우
    if (!file) {
        alert('파일을 선택해주세요.');
        return; // 함수를 빠져나가서 더 이상 진행하지 않음
    }

    var form = document.getElementById('uploadForm');
    var formData = new FormData(form);

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadProfileImage", true);
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4) {
            if (xhr.status == 200) {
                alert('프로필 등록에 성공했습니다.');
                previewProfileImage(fileInput); // 이 부분을 수정했습니다
            } else {
                alert('프로필 등록에 실패했습니다.');
            }
        }
    };
    xhr.send(formData);
}

function previewProfileImage(input) {
    var file = input.files[0];
    if (file) {
        var reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('profileImg').src = e.target.result;
        }
        reader.readAsDataURL(file);
    }
}
