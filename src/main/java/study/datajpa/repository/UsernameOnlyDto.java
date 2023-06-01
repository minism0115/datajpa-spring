package study.datajpa.repository;

public class UsernameOnlyDto {

    private final String username;

    // 이 생성자가 중요! 이 생성자의 파라미터명을 가지고 분석
    public UsernameOnlyDto(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }
}
