package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handles integration with external API to fetch posts and comments.
 */
@Component
@Slf4j
public class AuditionIntegrationClient {
  /** Error message for integration failures. */
  public static final String INTEGRATION_ERROR = "Integration Error";
  /** Endpoint for fetching comments. */
  public static final String COMMENTS_ENDPOINT = "/comments";
  /** Base URL for JSONPlaceholder API. */
  private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
  /** Endpoint for fetching posts. */
  private static final String POSTS_ENDPOINT = "/posts";
  /** RestTemplate for making HTTP requests. */
  private final transient RestTemplate restTemplate;

  /**
   * Constructor to inject RestTemplate dependency.
   *
   * @param restTemplate the RestTemplate instance
   */
  public AuditionIntegrationClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Fetches all posts from an external API.
   *
   * @return a list of posts
   */
  public List<AuditionPost> getPosts() {
    // DONE make RestTemplate call to get Posts from https://jsonplaceholder.typicode.com/posts
    try {
      String url = BASE_URL + POSTS_ENDPOINT;
      log.debug("Fetching all posts from {}", url);

      AuditionPost[] posts = restTemplate.getForObject(url, AuditionPost[].class);
      return posts == null ? Collections.emptyList() : Arrays.asList(posts);
    } catch (HttpClientErrorException e) {
      log.error("Client Error fetching posts", e);
      throw new SystemException("Error fetching posts", "Client Error", e.getStatusCode().value(),
          e);
    } catch (RestClientException e) {
      log.error("Unexpected error fetching posts", e);
      throw new SystemException("Failed to retrieve posts", INTEGRATION_ERROR,
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }

  /**
   * Fetches a post by its ID.
   *
   * @param id the ID of the post
   * @return the retrieved post
   */
  public AuditionPost getPostById(final String id) {
    // DONE get post by post ID call from https://jsonplaceholder.typicode.com/posts/
    String url = BASE_URL + POSTS_ENDPOINT + "/" + id;
    log.debug("Fetching post with id: {} from {}", id, url);
    try {
      var post = restTemplate.getForObject(url, AuditionPost.class);
      return post == null ? new AuditionPost() : post;
    } catch (final HttpClientErrorException e) {
      log.error("Error getting post by id: {}", id, e);
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        throw new SystemException("Cannot find a Post with id " + id, "Resource Not Found", 404, e);
      } else {
        throw new SystemException("Error while fetching post with id " + id, e.getStatusText(),
            e.getStatusCode().value(), e);
      }
    } catch (RestClientException e) {
      log.error("Unexpected error getting post by id: {}", id, e);
      throw new SystemException("Failed to retrieve post with id " + id,
          INTEGRATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }

  // DONE Write a method GET comments for a post from
  // https://jsonplaceholder.typicode.com/posts/{postId}/comments - the comments must be returned as part of the post.

  // DONE write a method. GET comments for a particular
  // Post from https://jsonplaceholder.typicode.com/comments?postId={postId}.
  // The comments are a separate list that needs to be returned
  // to the API consumers. Hint: this is not part of the AuditionPost pojo.
  /**
   * Fetches comments associated with a specific post.
   *
   * @param postId the ID of the post
   * @return a list of comments for the post
   */
  public List<Comment> getPostWithComments(@NotNull String postId) {
    try {
      String url = BASE_URL + POSTS_ENDPOINT + "/" + postId + COMMENTS_ENDPOINT;
      log.debug("Fetching comments for post id: {} from {}", postId, url);

      Comment[] comments = restTemplate.getForObject(url, Comment[].class);
      return comments == null ? Collections.emptyList() : Arrays.asList(comments);

    } catch (HttpClientErrorException e) {
      if (log.isErrorEnabled()) {
        log.error("Error fetching comments for post id: {}, status: {}, message: {}",
            postId, e.getStatusCode(), e.getResponseBodyAsString(), e);
      }
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Collections.emptyList();
      }

      throw new SystemException("Error while fetching comments for post " + postId,
          e.getStatusText(), e.getStatusCode().value(), e);
    } catch (RestClientException e) {
      log.error("Unexpected error fetching comments for post id: {}", postId, e);
      throw new SystemException("Failed to retrieve comments for post " + postId,
          INTEGRATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }

  /**
   * Fetches comments for a specific post using a query parameter.
   *
   * @param postId the ID of the post
   * @return a list of comments for the post
   */
  public List<Comment> getCommentsByPostIdQueryParam(@NotNull String postId) {
    try {
      /*
       * Addressing CVE-2024-22259.
       * BASE_URL - Domain name has been verified, not taking as input.
       */
      String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + COMMENTS_ENDPOINT)
          .queryParam("postId", postId)
          .toUriString();

      log.debug("Fetching comments with query param for post id: {} from {}", postId, url);

      Comment[] posts = restTemplate.getForObject(url, Comment[].class);
      return posts == null ? Collections.emptyList() : Arrays.asList(posts);
    } catch (HttpClientErrorException e) {
      if (log.isErrorEnabled()) {
        log.error(
            "Error fetching comments with query param for post id: {}, status: {}, message: {}",
            postId,
            e.getStatusCode(), e.getResponseBodyAsString(), e);
      }
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Collections.emptyList();
      }
      throw new SystemException("Error while fetching comments for post " + postId,
          e.getStatusText(),
          e.getStatusCode().value(), e);
    } catch (RestClientException e) {
      log.error("Unexpected error fetching comments with query param for post id: {}", postId, e);
      throw new SystemException("Failed to retrieve comments for post " + postId, INTEGRATION_ERROR,
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }
  }
}
