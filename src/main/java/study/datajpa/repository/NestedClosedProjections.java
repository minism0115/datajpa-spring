package study.datajpa.repository;

public interface NestedClosedProjections {

    // username은 최적화 해서 가지고 오지면 team에 대한 건 전체 조회
    String getUsername();
    TeamInfo getTeam();

    interface TeamInfo{
        String getName();
    }
}
