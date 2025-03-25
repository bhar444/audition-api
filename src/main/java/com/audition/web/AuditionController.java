package com.audition.web;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * REST Controller for handling posts and comments-related API requests.
 */

@RestController
public class AuditionController {
  /** Error message for Internal Server Error. */
  public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
  /** Error message for Bad Request. */
  public static final String BAD_REQUEST = "Bad Request";
  /** Logger instance for logging events. */
  private static final Logger LOG = LoggerFactory.getLogger(AuditionController.class);
  /** AuditionService instance for business logic operations. */
  private final transient AuditionService auditionService;

  /**
   * Constructor for AuditionController.
   *
   * @param auditionService the service handling business logic
   */
  public AuditionController(AuditionService auditionService) {
    this.auditionService = auditionService;
  }

  /**
   * Retrieves a list of posts with optional filters for userId and postId.
   *
   * @param userId the ID of the user (optional)
   * @param id the ID of the post (optional)
   * @return a list of filtered posts
   */
  @GetMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<AuditionPost> getPosts(
      @RequestParam(required = false) Integer userId,
      @RequestParam(required = false) Integer id
  ) {
    LOG.info("Retrieving posts with filters - userId: {}, id: {}",
        userId, id);
    try {
      return auditionService.applyFilters(userId, id);
    } catch (Exception e) {
      LOG.error("Error retrieving posts", e);
      throw new SystemException("Error retrieving posts", INTERNAL_SERVER_ERROR, 500, e);
    }
  }

  /**
   * Retrieves a post by its ID.
   *
   * @param postId the ID of the post
   * @return the retrieved post
   */
  @GetMapping(value = "/posts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public AuditionPost getPostsById(@PathVariable("id") @NotNull String postId) {
    if (StringUtils.isBlank(postId)) {
      throw new SystemException("Post ID cannot be null or empty", BAD_REQUEST, 400);
    }
    int id;
    try {
      id = Integer.parseInt(postId);
      if (id <= 0) {
        throw new SystemException("Post ID must be a positive integer", BAD_REQUEST, 400);
      }
    } catch (NumberFormatException e) {
      throw new SystemException("Invalid Post ID format: " + postId, BAD_REQUEST, 400, e);
    }
    try {
      return auditionService.getPostById(postId);
    } catch (HttpClientErrorException e) {  // Specific exception for HTTP client errors
      LOG.error("HTTP error retrieving post with ID: {}", postId, e);
      throw new SystemException("Error retrieving post: " + e.getStatusText(), "Client Error",
          e.getStatusCode().value(), e);
    } catch (ResourceAccessException e) {
      LOG.error("Network issue retrieving post with ID: {}", postId, e);
      throw new SystemException("Network error while retrieving post", "Service Unavailable", 503,
          e);
    } catch (SystemException e) {
      throw e;
    } catch (Exception e) {  // Catch-all only if necessary, but log and rethrow properly
      LOG.error("Unexpected error retrieving post with ID: {}", postId, e);
      throw new SystemException("Unexpected error retrieving post", INTERNAL_SERVER_ERROR, 500, e);
    }

  }

  /**
   * Retrieves comments for a given post.
   *
   * @param postId the ID of the post
   * @return a list of comments
   */
  // DONE Add additional methods to return comments for each post. Hint: Check https://jsonplaceholder.typicode.com/
  @GetMapping(value = "/posts/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Comment> getCommentsForPost(@PathVariable("id") String postId) {
    var isNumeric = StringUtils.isNumeric(postId);
    if (postId == null || postId.isEmpty() || !isNumeric) {
      throw new SystemException("Post ID is in invalid format, must be a number", BAD_REQUEST, 400);
    }
    try {
      List<Comment> comments = auditionService.getPostWithComments(postId);
      if (comments == null) {
        comments = Collections.emptyList();
      }
      return comments;
    } catch (HttpClientErrorException e) {  // Specific exception for HTTP client errors
      LOG.error("HTTP error retrieving comments for post ID: {}", postId, e);
      throw new SystemException("Error retrieving comments: " + e.getStatusText(), "Client Error",
          e.getStatusCode().value(), e);
    } catch (ResourceAccessException e) {  // Network-related issues
      LOG.error("Network issue retrieving comments for post ID: {}", postId, e);
      throw new SystemException("Network error while retrieving comments", "Service Unavailable",
          503, e);
    } catch (SystemException e) {  // Re-throw custom exceptions
      throw e;
    } catch (Exception e) {  // Catch-all as a last resort
      LOG.error("Unexpected error retrieving comments for post ID: {}", postId, e);
      throw new SystemException("Unexpected error retrieving comments", "Internal Server Error",
          500, e);
    }
  }

  /**
   * Retrieves comments using query parameters.
   *
   * @param postId the ID of the post
   * @return a list of comments
   */
  @GetMapping(value = "/posts/comments", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<Comment> getCommentsByPostId(
      @RequestParam(value = "postId", required = true) String postId) {
    LOG.info("Calling Method getCommentsByPostId with postId: {}", postId);
    if (postId == null || postId.isEmpty() || !StringUtils.isNumeric(postId)) {
      throw new SystemException("Post ID cannot be empty or non-numeric", BAD_REQUEST, 400);
    }
    try {
      List<Comment> comments = auditionService.getCommentsByPostIdQueryParam(postId);
      if (comments == null) {
        comments = Collections.emptyList();
      }
      return comments;
    } catch (HttpClientErrorException e) {  // Specific exception for HTTP client errors
      LOG.error("HTTP error retrieving comments for post ID: {}", postId, e);
      throw new SystemException("Error retrieving comments: " + e.getStatusText(), "Client Error",
          e.getStatusCode().value(), e);
    } catch (ResourceAccessException e) {  // Network-related issues
      LOG.error("Network issue retrieving comments for post ID: {}", postId, e);
      throw new SystemException("Network error while retrieving comments", "Service Unavailable",
          503, e);
    } catch (SystemException e) {  // Re-throw custom exceptions
      throw e;
    } catch (Exception e) {  // Catch-all as a last resort
      LOG.error("Unexpected error retrieving comments for post ID: {}", postId, e);
      throw new SystemException("Unexpected error retrieving comments", "Internal Server Error",
          500, e);
    }
  }

}


