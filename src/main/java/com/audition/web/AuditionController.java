package com.audition.web;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * REST Controller for handling posts and comments-related API requests.
 */

@RestController
@Getter
public class AuditionController {

  /**
   * Error message for Internal Server Error.
   */
  public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
  /**
   * Error message for Bad Request.
   */
  public static final String BAD_REQUEST = "Bad Request";
  public static final String ERROR_RETRIEVING_COMMENTS = "Error retrieving comments: ";
  public static final String UNEXPECTED_ERROR_RETRIEVING_COMMENTS =
      "Unexpected error retrieving comments";
  public static final String CLIENT_ERROR = "Client Error";
  public static final String ERROR_RETRIEVING_POSTS = "Error retrieving posts";
  public static final String ERROR_RETRIEVING = "Error retrieving";
  /**
   * Logger instance for logging events.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AuditionController.class);
  /**
   * AuditionService instance for business logic operations.
   */
  private final AuditionService auditionService;

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
   * @param id     the ID of the post (optional)
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
      LOG.error(ERROR_RETRIEVING_POSTS, e);
      throw new SystemException(ERROR_RETRIEVING_POSTS, INTERNAL_SERVER_ERROR, 500, e);
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
    } catch (HttpStatusCodeException e) {
      throw new SystemException(ERROR_RETRIEVING_COMMENTS + e.getStatusText(), CLIENT_ERROR,
          e.getStatusCode().value(), e);
    } catch (Exception e) {
      throw new SystemException(UNEXPECTED_ERROR_RETRIEVING_COMMENTS, INTERNAL_SERVER_ERROR,
          500, e);
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
    } catch (HttpStatusCodeException e) {  // Specific exception for HTTP client errors
      throw new SystemException(ERROR_RETRIEVING_COMMENTS + e.getStatusText(), CLIENT_ERROR,
          e.getStatusCode().value(), e);
    } catch (Exception e) {
      throw new SystemException(UNEXPECTED_ERROR_RETRIEVING_COMMENTS, INTERNAL_SERVER_ERROR,
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
    } catch (HttpStatusCodeException e) {  // Specific exception for HTTP client errors
      throw new SystemException(ERROR_RETRIEVING_COMMENTS + e.getStatusText(), CLIENT_ERROR,
          e.getStatusCode().value(), e);
    } catch (Exception e) {
      throw new SystemException(UNEXPECTED_ERROR_RETRIEVING_COMMENTS, INTERNAL_SERVER_ERROR,
          500, e);
    }
  }

}


