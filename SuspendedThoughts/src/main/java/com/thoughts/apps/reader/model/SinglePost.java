package com.thoughts.apps.reader.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Daniel on 8/15/13.
 */
public class SinglePost {

    @Expose
    @SerializedName("ID")
    private String postID = null;
    public void setPostID(String postID) {
        this.postID = postID;
    }
    public String getPostID() {
        return postID;
    }

    @Expose
    @SerializedName("post_name")
    private String postSlug = null;
    public void setPostSlug(String postSlug) {
        this.postSlug = postSlug;
    }
    public String getPostSlug() {
        return postSlug;
    }

    @Expose
    @SerializedName("post_parent")
    private String postParent = null;
    public void setPostParent(String postParent) {
        this.postParent = postParent;
    }
    public String getPostParent() {
        return postParent;
    }

    @Expose
    @SerializedName("post_date")
    private String postDate = null;
    public void setPostDate(String postDate) {
        this.postDate = postID;
    }
    public String getPostDate() {
        return postDate;
    }

    @Expose
    @SerializedName("post_content")
    private String postContent = null;
    public void setPostContent(String postContent) {
        this.postDate = postContent;
    }
    public String getPostContent() {
        return postContent;
    }

    @Expose
    @SerializedName("post_category")
    private String postCategory = null;
    public void setPostCategory(String postCategory) {
        this.postCategory = postCategory;
    }
    public String getPostCategory() {
        return postCategory;
    }

    @Expose
    @SerializedName("post_title")
    private String postTitle = null;
    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }
    public String getPostTitle() {
        return postTitle;
    }

    @Expose
    @SerializedName("guid")
    private String postLink = null;
    public void getPostLink(String postLink) {
        this.postLink = postLink;
    }
    public String getPostLink() {
        return postLink;
    }

    @Expose
    @SerializedName("featured_img")
    private String featuredImgUrl = null;
    public void setFeaturedImgUrl(String featuredImgUrl) {
        this.featuredImgUrl = featuredImgUrl;
    }
    public String getFeaturedImgUrl() {
        return featuredImgUrl;
    }

    @Expose
    @SerializedName("post_type")
    private String postType = null;
    public void setPostType(String postType) {
        this.postType = postType;
    }
    public String getPostType() {
        return postType;
    }

    @Expose
    @SerializedName("videos")
    private String [] videos = null;
    public void setVideos(String [] videos) {
        this.videos = videos;
    }
    public String [] getVideos() {
        return videos;
    }

}
