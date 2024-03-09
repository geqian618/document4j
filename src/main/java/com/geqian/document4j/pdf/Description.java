package com.geqian.document4j.pdf;

/**
 * @author geqian
 * @date 15:18 2024/3/9
 */
public class Description {

    //文档标题
    private String title;

    // 文档作者
    private String author;

    // 文档创建时间
    private boolean createTime;

    // 文档主题
    private String subject;

    // 关键词
    private String keywords;

    // 创建者
    private String creator;

    private boolean producer;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isCreateTime() {
        return createTime;
    }

    public void setCreateTime(boolean createTime) {
        this.createTime = createTime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isProducer() {
        return producer;
    }

    public void setProducer(boolean producer) {
        this.producer = producer;
    }

    @Override
    public String toString() {
        return "Description{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", createTime=" + createTime +
                ", subject='" + subject + '\'' +
                ", keywords='" + keywords + '\'' +
                ", creator='" + creator + '\'' +
                ", producer=" + producer +
                '}';
    }
}
