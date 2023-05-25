package study.datajpa.entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Persistable<String> {

    @Id
//    @GeneratedValue // 자동 생성이 아니라 ID를 그냥 만든 경우 isNew로 판단하지 않기 때문에 persist가 아닌 merge가 된다
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    public Item(String id){
        this.id = id;
    }

    // ID를 직접 생성할 경우 Persistable을 implements 하고 아래의 메서드를 오버라이드 한다
    // 그런 다음 isNew를 직접 구현해야 한다
    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return createdDate == null;
    }
}
