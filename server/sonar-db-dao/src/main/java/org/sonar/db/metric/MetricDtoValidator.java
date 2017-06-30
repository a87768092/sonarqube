/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.db.metric;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class MetricDtoValidator {
  /*
   * Allowed characters are alphanumeric, '-', '_', with at least one non-digit
   */
  private static final String VALID_METRIC_KEY_REGEXP = "[\\p{Alnum}\\-_]*[\\p{Alpha}\\-_]+[\\p{Alnum}\\-_]*";
  private static final int MAX_KEY_LENGTH = 64;
  private static final int MAX_NAME_LENGTH = 64;
  private static final int MAX_DESCRIPTION_LENGTH = 255;
  private static final List<Consumer<MetricDto>> validators = ImmutableList.of(
    MetricDtoValidator::validateKey,
    MetricDtoValidator::validateName,
    MetricDtoValidator::validateDescription);

  private MetricDtoValidator() {
    // static utility methods only
  }

  static MetricDto validate(MetricDto metric) {
    validators.forEach(v -> v.accept(metric));
    return metric;
  }

  private static void validateKey(MetricDto metric) {
    String key = metric.getKey();
    checkArgument(!isNullOrEmpty(key), "Metric key cannot be empty");
    checkArgument(key.length() <= MAX_NAME_LENGTH, "Metric key length (%s) is longer than the maximum authorized (%s). '%s' was provided.",
      key.length(), MAX_KEY_LENGTH, key);
    validateKeyFormat(key);
  }

  private static void validateName(MetricDto metric) {
    String name = metric.getShortName();
    checkArgument(!isNullOrEmpty(name), "Metric name cannot be empty");
    checkArgument(name.length() <= MAX_NAME_LENGTH, "Metric name length (%s) is longer than the maximum authorized (%s). '%s' was provided.",
      name.length(), MAX_NAME_LENGTH, name);
  }

  private static void validateDescription(MetricDto metric) {
    String description = metric.getDescription();
    if (description == null) {
      return;
    }

    checkArgument(description.length() <= MAX_DESCRIPTION_LENGTH, "Metric description length (%s) is longer than the maximum authorized (%s). '%s' was provided.",
      description.length(), MAX_DESCRIPTION_LENGTH, description);
  }

  /**
   * <p>Test if given parameter is valid for a metric key. Valid format is:</p>
   * <ul>
   * <li>Allowed characters:
   * <ul>
   * <li>Uppercase ASCII letters A-Z</li>
   * <li>Lowercase ASCII letters a-z</li>
   * <li>ASCII digits 0-9</li>
   * <li>Punctuation signs dash '-', underscore '_'</li>
   * </ul>
   * </li>
   * <li>At least one non-digit</li>
   * </ul>
   *
   * @param candidateKey
   * @return <code>true</code> if <code>candidateKey</code> can be used for a metric
   */
  @VisibleForTesting
  static boolean isKeyValid(String candidateKey) {
    return candidateKey.matches(VALID_METRIC_KEY_REGEXP);
  }

  private static void validateKeyFormat(String candidateKey) {
    checkArgument(isKeyValid(candidateKey), "Malformed metric key '%s'. Allowed characters are alphanumeric, '-', '_', with at least one non-digit.", candidateKey);
  }
}
