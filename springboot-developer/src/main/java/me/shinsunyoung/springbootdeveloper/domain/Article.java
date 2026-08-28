package me.shinsunyoung.springbootdeveloper.domain;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "author", nullable = false)
    private String author;

    @Builder // construct a object as builder patterns
    public Article(String author, String title, String content, String imageUrl) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public void update(String title, String content, String imageUrlString) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    @CreatedDate // save created date when build entity
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate // save updated date when entity updated
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
