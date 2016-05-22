/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.blog;

import java.time.LocalDate;

import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.EmbeddedDocument;

/**
 * A blog post comment.
 */
@EmbeddedDocument
public class Comment {

  @DocumentField
  private String authorName;

  @DocumentField
  private String comment;

  @DocumentField
  private int stars;

  @DocumentField
  private LocalDate date;

  /**
   * Empty constructor.
   */
  public Comment() {
  }

  
  /**
   * Full constructor.
   * @param authorName the comment author
   * @param comment the comment content
   * @param stars the number of stars given to the parent {@link Blogpost}
   * @param date the date of the comment
   */
  public Comment(String authorName, String comment, int stars, LocalDate date) {
    this.authorName = authorName;
    this.comment = comment;
    this.stars = stars;
    this.date = date;
  }


  /**
   * @return the authorName.
   */
  public String getAuthorName() {
    return authorName;
  }

  /**
   * @param authorName the authorName to set.
   */
  public void setAuthorName(String authorName) {
    this.authorName = authorName;
  }

  /**
   * @return the comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * @param comment the comment to set.
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * @return the stars.
   */
  public int getStars() {
    return stars;
  }

  /**
   * @param stars the stars to set.
   */
  public void setStars(int stars) {
    this.stars = stars;
  }

  /**
   * @return the date.
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * @param date the date to set.
   */
  public void setDate(LocalDate date) {
    this.date = date;
  }

  @Override
  public String toString() {
    return "Comment [authorName=" + authorName + ", comment=" + comment + ", stars=" + stars
        + ", date=" + date + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((authorName == null) ? 0 : authorName.hashCode());
    result = prime * result + ((comment == null) ? 0 : comment.hashCode());
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + stars;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Comment other = (Comment) obj;
    if (authorName == null) {
      if (other.authorName != null) {
        return false;
      }
    } else if (!authorName.equals(other.authorName)) {
      return false;
    }
    if (comment == null) {
      if (other.comment != null) {
        return false;
      }
    } else if (!comment.equals(other.comment)) {
      return false;
    }
    if (date == null) {
      if (other.date != null) {
        return false;
      }
    } else if (!date.equals(other.date)) {
      return false;
    }
    if (stars != other.stars) {
      return false;
    }
    return true;
  }
  
}
