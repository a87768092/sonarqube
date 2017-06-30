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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.google.common.base.Strings.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.db.metric.MetricDtoValidator.validate;
import static org.sonar.db.metric.MetricTesting.newMetricDto;

public class MetricValidatorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private MetricDto metric = newMetricDto();

  @Test
  public void valid_metric() {
    metric
      .setKey(repeat("a", 64))
      .setShortName(repeat("b", 64))
      .setDescription(repeat("c", 255));

    validate(metric);
  }

  @Test
  public void isMetricKeyValid() {
    assertThat(MetricDtoValidator.isKeyValid("")).isFalse();
    assertThat(MetricDtoValidator.isKeyValid("1_2_3-ABC-1_2_3")).isTrue();
    assertThat(MetricDtoValidator.isKeyValid("123_321")).isTrue();
    assertThat(MetricDtoValidator.isKeyValid("123456")).isFalse();
    assertThat(MetricDtoValidator.isKeyValid("1.2.3_A_3:2:1")).isFalse();
  }

  @Test
  public void fail_when_invalid_key_format() {
    metric.setKey("123456");

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Malformed metric key '123456'. Allowed characters are alphanumeric, '-', '_', with at least one non-digit.");

    validate(metric);
  }

  @Test
  public void fail_if_key_longer_than_64_characters() {
    String a65 = repeat("a", 65);
    metric.setKey(a65);

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Metric key length (65) is longer than the maximum authorized (64). '" + a65 + "' was provided.");

    validate(metric);
  }

  @Test
  public void fail_if_name_longer_than_64_characters() {
    String a65 = repeat("a", 65);
    metric.setShortName(a65);

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Metric name length (65) is longer than the maximum authorized (64). '" + a65 + "' was provided.");

    validate(metric);
  }

  @Test
  public void fail_if_description_longer_than_255_characters() {
    String a256 = repeat("a", 256);
    metric.setDescription(a256);

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Metric description length (256) is longer than the maximum authorized (255). '" + a256 + "' was provided.");

    validate(metric);
  }
}
