package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for Audition application.
 */
@Service
public class AuditionService {

  public static final String USER_ID = "userId";

  /**
   * The AuditionIntegrationClient instance.
   */
  @Autowired
  private final transient  AuditionIntegrationClient auditionIntegrationClient;

  /**
   * Constructor for AuditionService.
   *
   */
  public AuditionService(AuditionIntegrationClient auditionIntegrationClient) {
    this.auditionIntegrationClient = auditionIntegrationClient;
  }


  public List<AuditionPost> getPosts() {
    return auditionIntegrationClient.getPosts();
  }

  /**
   * Filters the posts based on the userId and id.
   *
   */
  public AuditionPost getPostById(final String postId) {
    return auditionIntegrationClient.getPostById(postId);
  }

  /**
   * Get the post with comments based on the postId.
   *
   */
  public List<Comment> getPostWithComments(final String postId) {
    return auditionIntegrationClient.getPostWithComments(postId);
  }

  /**
   * Get the comments based on the postId.
   *
   */
  public List<Comment> getCommentsByPostIdQueryParam(final String postId) {
    return auditionIntegrationClient.getCommentsByPostIdQueryParam(postId);
  }

  /**
   * Filters the posts based on the userId and id.
   */
  public List<AuditionPost> applyFilters(Integer userId, Integer id) {

    return getPosts().stream()
        .filter(post -> userId == null || post.getUserId() == userId)
        .filter(post -> id == null || post.getId() == id)
        .collect(Collectors.toList());
  }

}