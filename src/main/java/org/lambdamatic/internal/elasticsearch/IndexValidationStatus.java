/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch;

/**
 * The validation status of an ES Index.
 */
public enum IndexValidationStatus {

  /** if the status is OK. */
  OK,
  /** if the current index mapping is invalid compared to the domain type metadata. */
  MAPPING_INVALID;

}
