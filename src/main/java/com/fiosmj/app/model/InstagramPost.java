package com.fiosmj.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "instagram_posts")
public class InstagramPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;     // URL da imagem (pode ser external ou /uploads/)
    @Column(length = 500)
    private String caption;
    private String postUrl;      // link para o post no Instagram
    private int displayOrder = 0;
    private boolean active = true;

    public InstagramPost() {}

    public Long getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getPostUrl() { return postUrl; }
    public void setPostUrl(String postUrl) { this.postUrl = postUrl; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
