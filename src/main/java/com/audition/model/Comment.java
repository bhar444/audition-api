package com.audition.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Represents a comment in the Audition system.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

  private int postId;
  private int id;
  private String name;
  private String email;
  private String body;

}
