package com.audition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import com.audition.web.AuditionController;
import java.util.Collections;
import java.util.List;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@Nested
@WebMvcTest(AuditionController.class)
@NoArgsConstructor
@ExtendWith(MockitoExtension.class)
class AuditionControllerTest {

  public static final String ABC = "abc";
  public static final String NOT_FOUND = "Not Found";
  public static final String ERROR_MESSAGE_SHOULD_MATCH = "Error message should match";
  public static final String STATUS_CODE_MATCH = "Status code should match";
  private static final String VALID_ID = "1";
  private static final String INVALID_ID = ABC;
  private static final String NEGATIVE_ID = "-1";
  @SuppressWarnings("PMD.LoggerIsNotStaticFinal")
  @Mock
  private Logger log;
  @Mock
  private AuditionService auditionService;
  @InjectMocks
  private AuditionController auditionController;

  @BeforeEach
  void setUp() {
    reset(auditionService, log);
    auditionController = new AuditionController(auditionService);
  }

  @Test
  void testGetPosts() {
    List<AuditionPost> posts = Collections.singletonList(new AuditionPost());
    when(auditionService.applyFilters(null, null)).thenReturn(posts);

    List<AuditionPost> result = auditionController.getPosts(null, null);

    assertEquals(posts, result, "Posts should match");
    verify(auditionService).applyFilters(null, null);
  }

  @Test
  @SuppressWarnings("PMD.UnusedLocalVariable")
  void testGetPostsException() {
    when(auditionService.applyFilters(null, null)).thenThrow(new RuntimeException("Error"));

    SystemException exception =
        assertThrows(SystemException.class, () -> auditionController.getPosts(null, null));

    assertEquals("Error retrieving posts", exception.getMessage(), ERROR_MESSAGE_SHOULD_MATCH);
    assertEquals(500, exception.getStatusCode(), STATUS_CODE_MATCH);
  }

  @Test
  void testGetPostsById() {
    AuditionPost post = new AuditionPost();
    when(auditionService.getPostById(VALID_ID)).thenReturn(post);

    AuditionPost result = auditionController.getPostsById(VALID_ID);

    assertEquals(post, result, "Post should match");
    verify(auditionService).getPostById(VALID_ID);
  }

  @Test
  void testGetPostsByIdInvalidId() {
    SystemException exception =
        assertThrows(SystemException.class, () -> auditionController.getPostsById(ABC));

    assertEquals("Invalid Post ID format: abc", exception.getMessage(), ERROR_MESSAGE_SHOULD_MATCH);
    assertEquals(400, exception.getStatusCode(), STATUS_CODE_MATCH);
  }



  @Test
  void testGetCommentsForPost() {
    List<Comment> comments = Collections.singletonList(new Comment());
    when(auditionService.getPostWithComments(VALID_ID)).thenReturn(comments);

    List<Comment> result = auditionController.getCommentsForPost(VALID_ID);

    assertEquals(comments, result, "Comments should match");
    verify(auditionService).getPostWithComments(VALID_ID);
  }

  @Test
  void testGetCommentsForPostInvalidId() {
    SystemException exception = assertThrows(SystemException.class,
        () -> auditionController.getCommentsForPost(ABC));

    assertEquals("Post ID is in invalid format, must be a number", exception.getMessage(), ERROR_MESSAGE_SHOULD_MATCH);
    assertEquals(400, exception.getStatusCode(), STATUS_CODE_MATCH);
  }



  @Test
  void testGetCommentsByPostId() {
    List<Comment> comments = Collections.singletonList(new Comment());
    when(auditionService.getCommentsByPostIdQueryParam(VALID_ID)).thenReturn(comments);

    List<Comment> result = auditionController.getCommentsByPostId(VALID_ID);

    assertEquals(comments, result, "Comments should match");
    verify(auditionService).getCommentsByPostIdQueryParam(VALID_ID);
  }

  @Test
  void testGetCommentsByPostIdInvalidId() {
    SystemException exception = assertThrows(SystemException.class,
        () -> auditionController.getCommentsByPostId(ABC));

    assertEquals("Post ID cannot be empty or non-numeric", exception.getMessage(), ERROR_MESSAGE_SHOULD_MATCH);
    assertEquals(400, exception.getStatusCode(), STATUS_CODE_MATCH);
  }


  @Test
  void testGetPostByIdShouldReturnPost() {
    // Given
    AuditionPost mockPost = new AuditionPost();
    mockPost.setId(1);
    mockPost.setTitle("Test Post");
    mockPost.setBody("Test Body");

    when(auditionService.getPostById(VALID_ID)).thenReturn(mockPost);

    // When
    AuditionPost result = auditionController.getPostsById(VALID_ID);

    // Then
    assertNotNull(result, "Post should not be null");
    assertEquals(1, result.getId(), "Post ID should match");
    assertEquals("Test Post", result.getTitle(), "Title should match");
    assertEquals("Test Body", result.getBody(), "Body should match");

    verify(auditionService, times(1)).getPostById(VALID_ID);
  }

  @Test
  void testGetPostByIdShouldThrowBadRequestWhenIdIsBlank() {
    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionController.getPostsById(""),
        "Should throw SystemException for blank ID"
    );

    assertEquals("Post ID cannot be null or empty", exception.getMessage(), ERROR_MESSAGE_SHOULD_MATCH);
    assertEquals(400, exception.getStatusCode(), STATUS_CODE_MATCH);
  }

  @Test
  void testGetPostByIdShouldThrowBadRequestWhenIdIsNotInteger() {
    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionController.getPostsById(INVALID_ID),
        "Should throw SystemException for non-integer ID"
    );

    assertEquals("Invalid Post ID format: abc", exception.getMessage(), ERROR_MESSAGE_SHOULD_MATCH);
    assertEquals(400, exception.getStatusCode(), STATUS_CODE_MATCH);
  }

  @Test
  void testGetPostByIdShouldThrowBadRequestWhenIdIsNegative() {
    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionController.getPostsById(NEGATIVE_ID),
        "Should throw SystemException for negative ID"
    );

    assertEquals("Post ID must be a positive integer", exception.getMessage(), ERROR_MESSAGE_SHOULD_MATCH);
    assertEquals(400, exception.getStatusCode(), STATUS_CODE_MATCH);
  }



}



