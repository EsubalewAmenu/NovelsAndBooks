package com.herma.apps.novelsandbooks.usefull;

public class PostItem {

  private int realId, id, blogposts_count, blogwriter_id;
  private String categoryName, blogwriter_name, chapterName, content;


  public int getRealId() {
    return realId;
  }

  public void setRealId(int realId) {
    this.realId = realId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getBlogposts_count() {
    return blogposts_count;
  }

  public void setBlogposts_count(int blogposts_count) {
    this.blogposts_count = blogposts_count;
  }

  public int getBlogwriter_id() {
    return blogwriter_id;
  }

  public void setBlogwriter_id(int blogwriter_id) {
    this.blogwriter_id = blogwriter_id;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getBlogwriter_name() {
    return blogwriter_name;
  }

  public void setBlogwriter_name(String blogwriter_name) {
    this.blogwriter_name = blogwriter_name;
  }

  public String getChapterName() {
    return chapterName;
  }

  public void setChapterName(String chapterName) {
    this.chapterName = chapterName;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
