package kopo.poly.repository;

import kopo.poly.repository.entity.UserInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoEntity, String> {

    // 회원 정보 조회(회원정보 화면, 회원가입 여부 등 다양하게 활용됨)
    // java.util.Optional 객체는 자바의 NullPointer 에러에 대응하기 위해 1.8버전부터 추가된 자바 객체
    // 객체에 값이 존재하는지 체크할 때 활용 가능한
    // 쿼리 예 : SELECT * FROM USER_INFO WHERE USER_ID = 'hglee67'
    Optional<UserInfoEntity> findByUserId(String userId);

    // 로그인
    // 쿼리 예 : SELECT * FROM USER_INFO WHERE USER_ID = 'hglee67' AND PASSWORD = '1234'
    Optional<UserInfoEntity> findByUserIdAndPassword(String userId, String password);

    // 아이디찾기
    Optional<UserInfoEntity> findByUserNameAndEmail(String userName, String email);

    // 이메일 중복체크
    Optional<UserInfoEntity> findByEmail(String email);

    // 비밀번호찾기
    UserInfoEntity findByUserIdAndEmailAndUserName(String userId, String email, String userName);

}
