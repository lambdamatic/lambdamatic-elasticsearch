/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.blog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentId;

/**
 * A blog post.
 */
@Document(index = "blogpost_index", type = "blogpost")
public class Blogpost {

  @DocumentId
  private Long id;

  @DocumentField
  private String title;

  @DocumentField
  private String body;

  @DocumentField
  private String[] tags;

  @DocumentField
  private List<Comment> comments;

  /**
   * @return the id.
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the body.
   */
  public String getBody() {
    return body;
  }

  /**
   * @param body the body to set.
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * @return the tags.
   */
  public String[] getTags() {
    return tags;
  }

  /**
   * @param tags the tags to set.
   */
  public void setTags(String[] tags) {
    this.tags = tags;
  }

  /**
   * @return the comments.
   */
  public List<Comment> getComments() {
    return comments;
  }

  /**
   * @param comments the comments to set.
   */
  public void setComments(List<Comment> comments) {
    if (this.comments == null) {
      this.comments = new ArrayList<>();
    }
    this.comments.clear();
    this.comments.addAll(comments);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((comments == null) ? 0 : comments.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + Arrays.hashCode(tags);
    result = prime * result + ((title == null) ? 0 : title.hashCode());
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
    Blogpost other = (Blogpost) obj;
    if (body == null) {
      if (other.body != null) {
        return false;
      }
    } else if (!body.equals(other.body)) {
      return false;
    }
    if (comments == null) {
      if (other.comments != null) {
        return false;
      }
    } else if (!comments.equals(other.comments)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (!Arrays.equals(tags, other.tags)) {
      return false;
    }
    if (title == null) {
      if (other.title != null) {
        return false;
      }
    } else if (!title.equals(other.title)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final int maxLen = 2;
    StringBuilder builder = new StringBuilder();
    builder.append("BlogPost [");
    if (id != null) {
      builder.append("id=").append(id).append(", ");
    }
    if (title != null) {
      builder.append("title=").append(title).append(", ");
    }
    if (body != null) {
      builder.append("body=").append(body).append(", ");
    }
    if (tags != null) {
      builder.append("tags=").append(Arrays.asList(tags).subList(0, Math.min(tags.length, maxLen)))
          .append(", ");
    }
    if (comments != null) {
      builder.append("comments=").append(comments.subList(0, Math.min(comments.size(), maxLen)));
    }
    builder.append("]");
    return builder.toString();
  }



}
