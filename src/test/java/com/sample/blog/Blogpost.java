/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.blog;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentIdField;
import org.lambdamatic.elasticsearch.annotations.FullText;
import org.lambdamatic.elasticsearch.annotations.Keyword;

/**
 * A blog post.
 */
@Document(index = Blogposts.BLOGPOST_INDEX_NAME, type = "blogpost")
public class Blogpost {

  @DocumentIdField
  private Long id;

  @DocumentField
  @FullText
  private String title;

  @DocumentField
  @FullText
  private String content;

  @DocumentField
  private String[] tags;

  @DocumentField
  private List<Comment> comments;

  @DocumentField
  @Keyword
  private BlogpostStatus status;

  @DocumentField(name = "publish_date")
  private LocalDate publishDate;

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
   * @return the content.
   */
  public String getContent() {
    return content;
  }

  /**
   * @param content the content to set.
   */
  public void setContent(String content) {
    this.content = content;
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
  public void setComments(final List<Comment> comments) {
    if (this.comments == null) {
      this.comments = new ArrayList<>();
    }
    this.comments.clear();
    if(comments != null) {
      this.comments.addAll(comments);
    }
  }

  public BlogpostStatus getStatus() {
    return this.status;
  }

  public void setStatus(BlogpostStatus status) {
    this.status = status;
  }

  public LocalDate getPublishDate() {
    return this.publishDate;
  }

  public void setPublishDate(LocalDate publishDate) {
    this.publishDate = publishDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((comments == null) ? 0 : comments.hashCode());
    result = prime * result + ((content == null) ? 0 : content.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((publishDate == null) ? 0 : publishDate.hashCode());
    result = prime * result + ((status == null) ? 0 : status.hashCode());
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
    if (comments == null) {
      if (other.comments != null) {
        return false;
      }
    } else if (!comments.equals(other.comments)) {
      return false;
    }
    if (content == null) {
      if (other.content != null) {
        return false;
      }
    } else if (!content.equals(other.content)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (publishDate == null) {
      if (other.publishDate != null) {
        return false;
      }
    } else if (!publishDate.equals(other.publishDate)) {
      return false;
    }
    if (status != other.status) {
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
    return "Blogpost [id=" + id + ", title=" + title + ", content=" + content + ", tags="
        + Arrays.toString(tags) + ", comments=" + comments + ", status=" + status + ", publishDate="
        + publishDate + "]";
  }


}
