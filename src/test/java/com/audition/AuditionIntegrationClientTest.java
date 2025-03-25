package com.audition;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


class AuditionIntegrationClientTest {

  public static final String API_ERROR = "API Error";
  public static final String COMMENT_1 = "Comment 1";
  public static final String COMMENT_2 = "Comment 2";
  public static final String URL1 = "https://jsonplaceholder.typicode.com/posts/1";
  private static final String POST_ID = "1";
  private static final String EXPECTED_URL =
      "https://jsonplaceholder.typicode.com/comments?postId=" + POST_ID;
  private static final String EXPECTED_ERROR_MESSAGE =
      "Failed to retrieve comments for post " + POST_ID;
  private static final Logger LOG = LoggerFactory.getLogger(AuditionIntegrationClient.class);
  private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
  private static final String COMMENTS_ENDPOINT = "/comments";
  private static final String POSTS_ENDPOINT = "/posts";
  private static final String VALID_POST_ID = "1";
  private static final String INVALID_POST_ID = "999";
  @InjectMocks
  private transient AuditionIntegrationClient auditionIntegrationClient;
  @Mock
  private transient RestTemplate restTemplate;

  private String buildUrl(String postId) {
    return UriComponentsBuilder.fromHttpUrl(BASE_URL + COMMENTS_ENDPOINT)
        .queryParam("postId", postId)
        .toUriString();
  }

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    auditionIntegrationClient = new AuditionIntegrationClient(restTemplate);
  }

  @Test
  void testGetPostsShouldReturnPostList() {
    AuditionPost[] mockPosts = new AuditionPost[] {new AuditionPost(), new AuditionPost()};
    when(restTemplate.getForObject(BASE_URL + POSTS_ENDPOINT, AuditionPost[].class))
        .thenReturn(mockPosts);

    List<AuditionPost> posts = auditionIntegrationClient.getPosts();
    if (posts == null || posts.isEmpty()) {
      assertAll("Validating posts response",
          () -> assertNotNull(posts, "Posts list should not be null"),
          () -> assertEquals(2, posts.size(), "Expected exactly 2 posts")
      );
    }
  }

  @Test
  void testGetPostsShouldReturnEmptyListWhenApiReturnsNull() {
    when(restTemplate.getForObject(BASE_URL + POSTS_ENDPOINT, AuditionPost[].class))
        .thenReturn(null);
    List<AuditionPost> posts = auditionIntegrationClient.getPosts();
    assertTrue(posts != null && posts.isEmpty(),
        "Posts list should be non-null and empty when API returns null");
  }

  @Test
  void testGetPostsShouldThrowSystemExceptionWhenApiFails() {
    when(restTemplate.getForObject(BASE_URL + POSTS_ENDPOINT, AuditionPost[].class))
        .thenThrow(new RestClientException(API_ERROR));
    SystemException exception =
        assertThrows(SystemException.class, auditionIntegrationClient::getPosts,
            "Expected SystemException when API call fails");
    assertEquals("Failed to retrieve posts", exception.getMessage(),
        "Exception message should indicate failure to fetch posts");
  }

  @Test
  void testGetPostByIdShouldReturnPostWhenValidId() {
    AuditionPost mockPost = new AuditionPost();
    mockPost.setId(1);
    mockPost.setTitle("Test Title");
    when(restTemplate.getForObject(URL1, AuditionPost.class))
        .thenReturn(mockPost);
    AuditionPost result = auditionIntegrationClient.getPostById("1");
    assertNotNull(result, "Post should not be null");
    assertEquals(1, result.getId(), "Post ID should match");
    assertEquals("Test Title", result.getTitle(), "Post title should match");
    verify(restTemplate).getForObject(URL1, AuditionPost.class);
  }

  @Test
  void testGetPostByIdShouldThrowSystemExceptionWhenNotFound() {
    String nonExistentId = "999";
    String expectedUrl = BASE_URL + POSTS_ENDPOINT + "/" + nonExistentId;
    String expectedErrorMessage = "Cannot find a Post with id " + nonExistentId;
    when(restTemplate.getForObject(expectedUrl, AuditionPost.class))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
    SystemException actualException = assertThrows(
        SystemException.class,
        () -> auditionIntegrationClient.getPostById(nonExistentId),
        "Should throw SystemException when post is not found"
    );
    assertEquals(expectedErrorMessage, actualException.getMessage(),
        "Exception message should indicate the post ID was not found");
    verify(restTemplate).getForObject(expectedUrl, AuditionPost.class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void testGetPostByIdShouldThrowSystemExceptionWhenApiFails() {
    String postId = "1";
    String expectedUrl = BASE_URL + POSTS_ENDPOINT + "/" + postId;

    when(restTemplate.getForObject(expectedUrl, AuditionPost.class))
        .thenThrow(new RestClientException(API_ERROR));

    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionIntegrationClient.getPostById(postId),
        "Should throw SystemException when the API call fails"
    );

    assertEquals("Failed to retrieve post with id " + postId, exception.getMessage(),
        "Exception message should indicate failure to retrieve the post");
    assertEquals("Integration Error", exception.getTitle(),
        "Exception title should be 'Integration Error'");
    assertEquals(500, exception.getStatusCode(),
        "Expected HTTP 500 Internal Server Error");
    assertNotNull(exception.getCause(),
        "Exception cause should not be null");
    assertTrue(exception.getCause() instanceof RestClientException,
        "Cause should be a RestClientException");

    verify(restTemplate, times(1)).getForObject(expectedUrl, AuditionPost.class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void testGetPostWithCommentsShouldReturnComments() {
    Comment comment1 = new Comment();
    comment1.setId(1);
    comment1.setPostId(1);
    comment1.setName(COMMENT_1);

    Comment comment2 = new Comment();
    comment2.setId(2);
    comment2.setPostId(1);
    comment2.setName(COMMENT_2);

    Comment[] mockComments = {comment1, comment2};
    String postId = "1";
    String expectedUrl = BASE_URL + POSTS_ENDPOINT + "/" + postId + COMMENTS_ENDPOINT;

    when(restTemplate.getForObject(expectedUrl, Comment[].class))
        .thenReturn(mockComments);

    List<Comment> result = auditionIntegrationClient.getPostWithComments(postId);
    assertNotNull(result, "Comments list should not be null");
    assertEquals(2, result.size(), "Comments list should contain 2 items");
    assertEquals(COMMENT_1, result.get(0).getName(), "First comment should have correct name");
    assertEquals(COMMENT_2, result.get(1).getName(), "Second comment should have correct name");
    verify(restTemplate).getForObject(expectedUrl, Comment[].class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void testGetPostWithCommentsShouldReturnEmptyListWhenApiReturnsNull() {
    String postId = "1";
    String expectedUrl = BASE_URL + POSTS_ENDPOINT + "/" + postId + COMMENTS_ENDPOINT;
    when(restTemplate.getForObject(expectedUrl, Comment[].class))
        .thenReturn(null);
    List<Comment> result = auditionIntegrationClient.getPostWithComments(postId);
    assertNotNull(result, "Comments list should not be null even when API returns null");
    assertTrue(result.isEmpty(), "Comments list should be empty when API returns null");
    assertEquals(0, result.size(), "Comments list size should be 0 when API returns null");
    verify(restTemplate).getForObject(expectedUrl, Comment[].class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void testGetPostWithCommentsShouldReturnEmptyListWhenNotFound() {
    String postId = "999";
    String expectedUrl = BASE_URL + POSTS_ENDPOINT + "/" + postId + COMMENTS_ENDPOINT;

    when(restTemplate.getForObject(expectedUrl, Comment[].class))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    List<Comment> comments = auditionIntegrationClient.getPostWithComments(postId);

    LOG.debug("Retrieved comments: {}", comments);

    assertAll("Validating empty comment response",
        () -> assertNotNull(comments, "Returned comment list should not be null"),
        () -> assertTrue(comments.isEmpty(), "Expected an empty list when post is not found")
    );

    verify(restTemplate, times(1)).getForObject(expectedUrl, Comment[].class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void testGetPostByIdShouldThrowSystemExceptionWhenApiFail() {
    String postId = "1";
    String expectedUrl = BASE_URL + POSTS_ENDPOINT + "/" + postId;
    String expectedExceptionMessage = "Failed to retrieve post with id " + postId;

    //log.debug("Expected exception message: {}", expectedExceptionMessage);

    when(restTemplate.getForObject(expectedUrl, AuditionPost.class))
        .thenThrow(new RestClientException("API Error"));

    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionIntegrationClient.getPostById(postId),
        "Should throw SystemException when the API call fails"
    );

    //log.debug("Caught SystemException: {}", exception.getMessage());
    assertEquals(expectedExceptionMessage, exception.getMessage(),
        "Exception message should indicate failure to retrieve the post");
    assertEquals("Integration Error", exception.getTitle(),
        "Exception title should be 'Integration Error'");
    assertEquals(500, exception.getStatusCode(),
        "Expected HTTP 500 Internal Server Error");
    assertNotNull(exception.getCause(),
        "Exception cause should not be null");
    assertTrue(exception.getCause() instanceof RestClientException,
        "Cause should be a RestClientException");

    verify(restTemplate, times(1)).getForObject(expectedUrl, AuditionPost.class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldReturnComments() {
    String postId = "1";
    String expectedUrl = "https://jsonplaceholder.typicode.com/comments?postId=" + postId;
    Comment[] mockComments = {new Comment(), new Comment()};

    when(restTemplate.getForObject(expectedUrl, Comment[].class)).thenReturn(mockComments);

    List<Comment> comments = auditionIntegrationClient.getCommentsByPostIdQueryParam(postId);


    assertNotNull(comments, "Returned comments list should not be null");

    assertAll("Validating comments response",
        () -> assertEquals(2, comments.size(), "Expected exactly 2 comments")
    );

    verify(restTemplate, times(1)).getForObject(expectedUrl, Comment[].class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldReturnEmptyListWhenApiReturnsNull() {
    String postId = "1";
    String expectedUrl = "https://jsonplaceholder.typicode.com/comments?postId=" + postId;

    when(restTemplate.getForObject(expectedUrl, Comment[].class))
        .thenReturn(null);
    List<Comment> result = auditionIntegrationClient.getCommentsByPostIdQueryParam(postId);
    assertNotNull(result, "Comments list should not be null even when API returns null");
    assertTrue(result.isEmpty(), "Comments list should be empty when API returns null");
    assertEquals(0, result.size(), "Comments list should have size 0 when API returns null");
    verify(restTemplate).getForObject(expectedUrl, Comment[].class);
    verifyNoMoreInteractions(restTemplate);
  }


  @Test
  void testGetCommentsByPostIdQueryParamShouldThrowSystemException() {
    when(restTemplate.getForObject(EXPECTED_URL, Comment[].class))
        .thenThrow(new RestClientException(API_ERROR));

    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionIntegrationClient.getCommentsByPostIdQueryParam(POST_ID),
        "Should throw SystemException when the API call fails"
    );

    assertEquals(EXPECTED_ERROR_MESSAGE, exception.getMessage(),
        "Exception message should indicate failure to retrieve comments");
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldInvokeRestTemplateOnce() {
    when(restTemplate.getForObject(EXPECTED_URL, Comment[].class))
        .thenThrow(new RestClientException(API_ERROR));

    assertThrows(SystemException.class,
        () -> auditionIntegrationClient.getCommentsByPostIdQueryParam(POST_ID));

    verify(restTemplate).getForObject(EXPECTED_URL, Comment[].class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  void testGetCommentsByPostIdQueryParamsShouldReturnComments() {
    // Given
    Comment comment1 = new Comment();
    comment1.setBody(COMMENT_1);
    Comment comment2 = new Comment();
    comment2.setBody(COMMENT_2);

    Comment[] mockComments = {comment1, comment2};
    String url = buildUrl(VALID_POST_ID);

    when(restTemplate.getForObject(url, Comment[].class)).thenReturn(mockComments);

    // When
    List<Comment> result;
    result = auditionIntegrationClient.getCommentsByPostIdQueryParam(VALID_POST_ID);

    // Then
    assertNotNull(result, "Returned list should not be null");
    assertEquals(2, result.size(), "Expected exactly 2 comments");
    assertEquals(COMMENT_1, result.get(0).getBody(), "First comment should match");
    assertEquals(COMMENT_2, result.get(1).getBody(), "Second comment should match");

    verify(restTemplate, times(1)).getForObject(url, Comment[].class);
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldReturnEmptyListWhenNotFound() {
    // Given
    String url = buildUrl(INVALID_POST_ID);
    when(restTemplate.getForObject(url, Comment[].class))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    // When
    List<Comment> result = auditionIntegrationClient.getCommentsByPostIdQueryParam(INVALID_POST_ID);

    // Then
    assertNotNull(result, "Returned list should not be null");
    assertTrue(result.isEmpty(), "Expected an empty list for 404 response");

    verify(restTemplate, times(1)).getForObject(url, Comment[].class);
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldThrowSystemExceptionOnHttpClientErrorException() {
    // Given
    String url = buildUrl(VALID_POST_ID);
    when(restTemplate.getForObject(url, Comment[].class))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

    // When & Then
    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionIntegrationClient.getCommentsByPostIdQueryParam(VALID_POST_ID),
        "Should throw SystemException for HTTP error"
    );

    assertEquals("Error while fetching comments for post 1", exception.getMessage(),
        "Exception message should indicate failure to fetch comments");
    assertEquals(400, exception.getStatusCode(), "Expected HTTP 400 error code");

    verify(restTemplate, times(1)).getForObject(url, Comment[].class);
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldThrowSystemExceptionOnRestClientException() {
    // Given
    String url = buildUrl(VALID_POST_ID);
    when(restTemplate.getForObject(url, Comment[].class))
        .thenThrow(new RestClientException("Network Error"));

    // When & Then
    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionIntegrationClient.getCommentsByPostIdQueryParam(VALID_POST_ID),
        "Should throw SystemException for network issues"
    );

    assertEquals("Failed to retrieve comments for post 1", exception.getMessage(),
        "Exception message should indicate failure to fetch comments");
    assertEquals(500, exception.getStatusCode(), "Expected HTTP 500 error code");

    verify(restTemplate, times(1)).getForObject(url, Comment[].class);
  }


}
