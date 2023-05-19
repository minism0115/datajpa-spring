package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    @Query(name = "Member.findByUsername") // 이 부분을 주석처리 하더라도 정상 동작!
    List<Member> findByUsername(@Param("username") String username);

    // 실무에서 많이 쓰이는 좋은 방법
    // 어플리케이션 로딩 시점에 쿼리 파싱하며 오류 체크
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    // 실무에서 많이 쓴다.
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    List<Member> findListByUsername(String username); // 컬렉션
    Member findMemberByUsername(String username); // 단건
    Optional<Member> findOptionalByUsername(String username); // 단건 Optional

    // count 쿼리의 최적화를 위해 count 쿼리 분리 가능
    @Query(value = "select m from Member m left join m.team t",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findByAge(int age, Pageable pageable);

    // 이 어노테이션을 꼭 붙여줘야 executeUpdate 실행
    @Modifying(clearAutomatically = true) // 영속성 컨택스트 초기화 해주는 옵션
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member left join fetch m.team")
    List<Member> findMemberFetchJoin();

    @Override
    @EntityGraph(attributePaths = {"team"}) // fetch 조인을 JPQL 없이 간단하게 구현
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member")
    List<Member> findMemberEntityGraph();

    @EntityGraph(attributePaths = {"team"})
//    @EntityGraph("Member.all") // NamedEntityGraph 사용
    List<Member> findEntityGraphByUsername(@Param("username") String username);

}
