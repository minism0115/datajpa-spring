package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember(){
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);

    }

    @Test
    public void findByUsernameAndAgeGreaterThan(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("AAA", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(member1);
    }

    @Test
    public void testQuery(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> result = memberRepository.findUser("AAA", 10);
        assertThat(result.get(0)).isEqualTo(member1);
    }

    @Test
    public void findUsernameList(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto(){
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member1 = new Member("AAA", 10);
        member1.setTeam(team);
        memberRepository.save(member1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames(){
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member1 = new Member("AAA", 10);
        member1.setTeam(team);
        memberRepository.save(member1);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }
    @Test
    public void returnType(){
        Member member1 = new Member("AAA", 10);
        Member member2 = new Member("BBB", 20);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> aaa = memberRepository.findListByUsername("AAA");
    }

    @Test
    public void paging(){
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);

        // 페이징 결과를 반환할 때도 무조건 entity는 숨기고 dto로!
        Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        // then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    public void bulkUpdate() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);
//        em.flush(); // 변경사항 적용
//        em.clear(); // 영속성 컨택스트에 있는 내용 다 날려버림

        // 벌크 업데이트는 영속성 컨택스트를 무시하고 바로 DB값 변경
        List<Member> members = memberRepository.findByUsername("member5");
        Member member5 = members.get(0);
        System.out.println("member5 = " + member5); // age 41이 아니라 40

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy(){
        // given
        // member1 -> teamA
        // member2 -> teatB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // when
        List<Member> members = memberRepository.findAll();
        List<Member> memberFetchJoin = memberRepository.findMemberFetchJoin();

        for (Member member : members) {
            System.out.println("member: " + member.getUsername());
            System.out.println("member.teamClass: " + member.getTeam().getClass());
            System.out.println("member.team: " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint(){ // hibernate에 주는 힌트야
        // given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush(); // DB에 결과 넘기고 남아 있어
        em.clear(); // 영속성 컨텍스트 날아가

        // when
        Member findMember = memberRepository.findById(member1.getId()).get();
        // 더티 체킹이 일어난다는 것 자체가 이전 값을 가지고 있다는 의미 -> 비용 발생
        // 정말 조회용으로만 쓸 거라면,
//        findMember.setUsername("member2");

        Member findMemberByQueryHint = memberRepository.findReadOnlyByUsername("member1" );
        findMemberByQueryHint.setUsername("member2"); // findMemberByQueryHint는 변경 감지를 안 하기 때문에 변경 안 됨

        em.flush();
    }

    @Test
    public void lock(){
        // given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush(); // DB에 결과 넘기고 남아 있어
        em.clear(); // 영속성 컨텍스트 날아가

        // when
        List<Member> result = memberRepository.findLockByUsername("member1" );
    }

    @Test
    public void callCustom(){
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void specBasic(){
        // given
        Team teamA = new Team("teamA" );
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Specification<Member> spec = MemberSpec.username("m1" ).and(MemberSpec.teamName("teamA" ));
        List<Member> result = memberRepository.findAll(spec);

        // then
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void queryByExample(){
        // given
        Team teamA = new Team("teamA" );
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        // probe 생성
        // 도메인 객체를 가지고 조회 조건을 만들어버려
        // 연관관계를 만들면 이거 그대로 조회 조건을 만들어줘
        // 한계: inner join만 가능, left outer join 불가
        Member member = new Member("m1" );
        Team team = new Team("teamA" );
        member.setTeam(team);

        // 조회할 때 age는 무시해!
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("age");

        Example<Member> example = Example.of(member, matcher);

        List<Member> result = memberRepository.findAll(example);

        Assertions.assertThat(result.get(0).getUsername()).isEqualTo("m1");
    }

    @Test
    public void projections(){
        // given
        Team teamA = new Team("teamA" );
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1" );

        for (UsernameOnly usernameOnly : result) {
            System.out.println("usernameOnly: " + usernameOnly);
        }
    }

    @Test
    public void projectionsDto(){
        // given
        Team teamA = new Team("teamA" );
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        // 프록시 객체가 아니라 실제 클래스로 반환된다!
        List<UsernameOnlyDto> result = memberRepository.findProjectionsDtoByUsername("m1", UsernameOnlyDto.class);

        for (UsernameOnlyDto usernameOnly : result) {
            System.out.println("usernameOnly: " + usernameOnly);
        }
    }

    @Test
    public void projectionsNestedClosed(){
        // given
        Team teamA = new Team("teamA" );
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        // 프록시 객체가 아니라 실제 클래스로 반환된다!
        List<NestedClosedProjections> result = memberRepository.findProjectionsDtoByUsername("m1", NestedClosedProjections.class);

        for (NestedClosedProjections nestedClosedProjections : result) {
            String username = nestedClosedProjections.getUsername();
            System.out.println("username: " + username);
            String teamName = nestedClosedProjections.getTeam().getName();
            System.out.println("teamName: " + teamName);
        }
    }

    @Test
    public void nativeQuery(){
        // given
        Team teamA = new Team("teamA" );
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Member result = memberRepository.findByNativeQuery("m1" );
        System.out.println("result: " + result);
    }

    @Test
    public void nativeProjection(){
        // given
        Team teamA = new Team("teamA" );
        em.persist(teamA);

        Member m1 = new Member("m1", 0, teamA);
        Member m2 = new Member("m2", 0, teamA);
        em.persist(m1);
        em.persist(m2);

        em.flush();
        em.clear();

        // when
        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 10));
        List<MemberProjection> content = result.getContent();
        for (MemberProjection memberProjection : content) {
            System.out.println("username: " + memberProjection.getUsername());
            System.out.println("teamName: " + memberProjection.getTeamName());
        }
    }
}