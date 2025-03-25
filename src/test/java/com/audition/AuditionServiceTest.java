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
import com.audition.service.AuditionService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Getter
class AuditionServiceTest {

  public static final String RETURNED_COMMENT_LIST_SHOULD_NOT_BE_NULL = "Returned comment list should not be null";
  public static final String FAILED_TO_RETRIEVE_COMMENTS = "Failed to retrieve comments";

  @Mock
  private  AuditionIntegrationClient auditionIntegrationClient;

  private List<AuditionPost> samplePosts;


  @InjectMocks
  private AuditionService auditionService;

  @BeforeEach
  void setUp() {
    samplePosts = Arrays.asList(
        new AuditionPost(1, 101, "Title1", "Body1"),
        new AuditionPost(2, 102, "Title2", "Body2"),
        new AuditionPost(1, 103, "Title3", "Body3"),
        new AuditionPost(3, 104, "Title4", "Body4")
    );
  }

  @Test
  void testApplyFiltersNoFiltersShouldReturnAllPosts() {
    // Arrange
    when(auditionService.getPosts()).thenReturn(samplePosts);

    // Act
    List<AuditionPost> result = auditionService.applyFilters(null, null);

    // Assert
    assertEquals(4, result.size(), "Should return all posts when no filters are applied");
  }

  @Test
  void testApplyFiltersFilterByUserIdShouldReturnCorrectSize() {
    when(auditionService.getPosts()).thenReturn(samplePosts);

    List<AuditionPost> result = auditionService.applyFilters(1, null);

    assertEquals(2, result.size(), "Should return 2 posts for userId=1");
  }

  @Test
  void testApplyFiltersFirstPostShouldMatchUserId() {
    when(auditionService.getPosts()).thenReturn(samplePosts);

    List<AuditionPost> result = auditionService.applyFilters(1, null);

    assertEquals(1, result.get(0).getUserId(), "First post should match userId=1");
  }

  @Test
  void testApplyFiltersSecondPostShouldMatchUserId() {
    when(auditionService.getPosts()).thenReturn(samplePosts);

    List<AuditionPost> result = auditionService.applyFilters(1, null);

    assertEquals(1, result.get(1).getUserId(), "Second post should match userId=1");
  }



  @Test
  void testApplyFiltersNoMatchShouldReturnEmptyList() {
    when(auditionService.getPosts()).thenReturn(samplePosts);

    List<AuditionPost> result = auditionService.applyFilters(99, 999);

    assertEquals(0, result.size(), "Should return an empty list when no matches are found");
  }



  @Test
  void testGetPostsShouldReturnPostList() {
    // Given
    AuditionPost post1 = new AuditionPost(1, 1, "Title 1", "Body 1");
    AuditionPost post2 = new AuditionPost(2, 2, "Title 2", "Body 2");

    List<AuditionPost> expectedPosts = List.of(post1, post2);
    when(auditionIntegrationClient.getPosts()).thenReturn(expectedPosts);

    // When
    List<AuditionPost> result = auditionService.getPosts();

    // Then
    assertNotNull(result, "Result should not be null");
    assertEquals(2, result.size(), "Expected exactly 2 posts");
    assertEquals(expectedPosts, result, "Posts should match expected list");

    verify(auditionIntegrationClient, times(1)).getPosts();
    verifyNoMoreInteractions(auditionIntegrationClient);
  }


  @Test
  void testGetPostsShouldReturnEmptyListWhenIntegrationClientReturnsEmpty() {
    // Given
    when(auditionIntegrationClient.getPosts()).thenReturn(Collections.emptyList());

    // When
    List<AuditionPost> result = auditionService.getPosts();

    // Then
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Expected an empty list");

    verify(auditionIntegrationClient, times(1)).getPosts();
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetPostsShouldThrowExceptionWhenIntegrationClientFails() {
    // Given
    when(auditionIntegrationClient.getPosts()).thenThrow(new RuntimeException("API failure"));

    // When & Then
    RuntimeException exception = assertThrows(
        RuntimeException.class,
        auditionService::getPosts,
        "Should throw RuntimeException when integration client fails"
    );

    assertEquals("API failure", exception.getMessage(), "Exception message should match");
    verify(auditionIntegrationClient, times(1)).getPosts();
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetPostByIdShouldReturnPost() {
    // Given
    String postId = "1";
    AuditionPost expectedPost = new AuditionPost(1, 1, "Test Title", "Test Body");
    when(auditionIntegrationClient.getPostById(postId)).thenReturn(expectedPost);

    // When
    AuditionPost result = auditionService.getPostById(postId);

    // Then
    assertNotNull(result, "Returned post should not be null");
    assertEquals(expectedPost, result, "Returned post should match expected post");

    verify(auditionIntegrationClient, times(1)).getPostById(postId);
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetPostByIdShouldThrowExceptionWhenIntegrationClientFails() {
    // Given
    String postId = "1";
    when(auditionIntegrationClient.getPostById(postId))
        .thenThrow(new SystemException("Failed to retrieve post", "Service Error", 500));

    // When & Then
    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionService.getPostById(postId),
        "Should throw SystemException when integration client fails"
    );

    assertEquals("Failed to retrieve post", exception.getMessage(),
        "Exception message should match");
    verify(auditionIntegrationClient, times(1)).getPostById(postId);
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetPostWithCommentsShouldReturnComments() {
    // Given
    String postId = "1";
    List<Comment> expectedComments = List.of(
        new Comment(1, 1, "Test Author 1", "Test Comment 1", "Comment1"),
        new Comment(2, 1, "Test Author 2", "Test Comment 2", "Comment2")
    );

    when(auditionIntegrationClient.getPostWithComments(postId)).thenReturn(expectedComments);

    // When
    List<Comment> result = auditionService.getPostWithComments(postId);

    // Then
    assertNotNull(result, RETURNED_COMMENT_LIST_SHOULD_NOT_BE_NULL);
    assertEquals(2, result.size(), "Expected exactly 2 comments");
    assertEquals(expectedComments, result, "Returned comments should match expected comments");

    verify(auditionIntegrationClient, times(1)).getPostWithComments(postId);
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetPostWithCommentsShouldReturnEmptyListWhenNoComments() {
    // Given
    String postId = "1";
    when(auditionIntegrationClient.getPostWithComments(postId)).thenReturn(Collections.emptyList());

    // When
    List<Comment> result = auditionService.getPostWithComments(postId);

    // Then
    assertNotNull(result, RETURNED_COMMENT_LIST_SHOULD_NOT_BE_NULL);
    assertTrue(result.isEmpty(), "Expected an empty list when no comments are found");

    verify(auditionIntegrationClient, times(1)).getPostWithComments(postId);
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetPostWithCommentsShouldThrowExceptionWhenIntegrationClientFails() {
    // Given
    String postId = "1";
    when(auditionIntegrationClient.getPostWithComments(postId))
        .thenThrow(new SystemException(FAILED_TO_RETRIEVE_COMMENTS, "Service Error", 500));

    // When & Then
    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionService.getPostWithComments(postId),
        "Should throw SystemException when integration client fails"
    );

    assertEquals(FAILED_TO_RETRIEVE_COMMENTS, exception.getMessage(),
        " Exception message should match");
    verify(auditionIntegrationClient, times(1)).getPostWithComments(postId);
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldReturnComments() {
    // Given
    String postId = "1";
    List<Comment> expectedComments = List.of(
        new Comment(1, 1, "Test Author 1", "Test Comment 1", "Comment1"),
        new Comment(2, 1, "Test Author 2", "Test Comment 2", "Comment2")
    );

    when(auditionIntegrationClient.getCommentsByPostIdQueryParam(postId)).thenReturn(
        expectedComments);

    // When
    List<Comment> result = auditionService.getCommentsByPostIdQueryParam(postId);

    // Then
    assertNotNull(result, RETURNED_COMMENT_LIST_SHOULD_NOT_BE_NULL);
    assertEquals(2, result.size(), "Expected exactly 2 comments");
    assertEquals(expectedComments, result, "Returned comments should match expected comments");

    verify(auditionIntegrationClient, times(1)).getCommentsByPostIdQueryParam(postId);
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldReturnEmptyListWhenNoComments() {
    // Given
    String postId = "1";
    when(auditionIntegrationClient.getCommentsByPostIdQueryParam(postId)).thenReturn(
        Collections.emptyList());

    // When
    List<Comment> result = auditionService.getCommentsByPostIdQueryParam(postId);

    // Then
    assertNotNull(result, RETURNED_COMMENT_LIST_SHOULD_NOT_BE_NULL);
    assertTrue(result.isEmpty(), "Expected an empty list when no comments are found");

    verify(auditionIntegrationClient, times(1)).getCommentsByPostIdQueryParam(postId);
    verifyNoMoreInteractions(auditionIntegrationClient);
  }

  @Test
  void testGetCommentsByPostIdQueryParamShouldThrowExceptionWhenIntegrationClientFails() {
    // Given
    String postId = "1";
    when(auditionIntegrationClient.getCommentsByPostIdQueryParam(postId))
        .thenThrow(new SystemException(FAILED_TO_RETRIEVE_COMMENTS, "Service Error", 500));

    // When & Then
    SystemException exception = assertThrows(
        SystemException.class,
        () -> auditionService.getCommentsByPostIdQueryParam(postId),
        "Should throw SystemException when integration client fails"
    );

    assertEquals(FAILED_TO_RETRIEVE_COMMENTS, exception.getMessage(),
        "Exception message should match");
    verify(auditionIntegrationClient, times(1)).getCommentsByPostIdQueryParam(postId);
    verifyNoMoreInteractions(auditionIntegrationClient);
  }
}
