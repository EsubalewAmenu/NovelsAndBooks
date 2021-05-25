package com.herma.apps.novelsandbooks.usefull;

public class CategoryItem {
    public int id;
    public String categoryName;

    public CategoryItem(int _id, String _categoryName){
        this.id = _id;
        this.categoryName = _categoryName;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
