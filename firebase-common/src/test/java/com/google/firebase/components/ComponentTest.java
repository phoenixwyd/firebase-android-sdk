// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.components;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ComponentTest {
  interface TestInterface {}

  static class TestClass implements TestInterface {}

  private final ComponentFactory<TestClass> nullFactory = container -> null;

  @Test
  public void builder_shouldSetCorrectDefaults() {
    Component<TestClass> component =
        Component.builder(TestClass.class).factory(nullFactory).build();
    assertThat(component.getProvidedInterfaces()).containsExactly(TestClass.class);
    assertThat(component.isLazy()).isTrue();
    assertThat(component.isAlwaysEager()).isFalse();
    assertThat(component.isEagerInDefaultApp()).isFalse();
    assertThat(component.getDependencies()).isEmpty();
  }

  @Test
  public void alwaysEager_shouldProperlySetComponentInitialization() {
    Component<TestClass> component =
        Component.builder(TestClass.class).alwaysEager().factory(nullFactory).build();

    assertThat(component.isLazy()).isFalse();
    assertThat(component.isAlwaysEager()).isTrue();
    assertThat(component.isEagerInDefaultApp()).isFalse();
  }

  @Test
  public void eagerInDefaultApp_shouldProperlySetComponentInitialization() {
    Component<TestClass> component =
        Component.builder(TestClass.class).eagerInDefaultApp().factory(nullFactory).build();

    assertThat(component.isLazy()).isFalse();
    assertThat(component.isAlwaysEager()).isFalse();
    assertThat(component.isEagerInDefaultApp()).isTrue();
  }

  @Test
  public void uptatingInstantiationMultipleTimes_shouldThrow() {
    Component.Builder<TestClass> builder = Component.builder(TestClass.class).eagerInDefaultApp();

    try {
      builder.alwaysEager();
      fail("Expected exception not thrown.");
    } catch (IllegalStateException ex) {
      // success
    }
  }

  @Test
  public void add_shouldProperlyAddDependencies() {
    Component<TestClass> component =
        Component.builder(TestClass.class)
            .add(Dependency.required(List.class))
            .add(Dependency.optional(Integer.class))
            .add(Dependency.requiredProvider(Float.class))
            .add(Dependency.optionalProvider(Double.class))
            .factory(nullFactory)
            .build();

    assertThat(component.getDependencies()).hasSize(4);
    assertThat(component.getDependencies())
        .containsExactly(
            Dependency.required(List.class),
            Dependency.optional(Integer.class),
            Dependency.requiredProvider(Float.class),
            Dependency.optionalProvider(Double.class));
  }

  @Test
  public void addRequiredDependency_onSelf_shouldThrow() {
    try {
      Component.builder(TestClass.class).add(Dependency.required(TestClass.class));
      fail("Expected exception not thrown.");
    } catch (IllegalArgumentException ex) {
      // success.
    }
  }

  @Test
  public void addOptionalDependency_onSelf_shouldThrow() {
    try {
      Component.builder(TestClass.class).add(Dependency.optional(TestClass.class));
      fail("Expected exception not thrown.");
    } catch (IllegalArgumentException ex) {
      // success.
    }
  }

  @Test
  public void builder_withMultipleInterfaces_shouldProperlySetInterfaces() {
    Component<TestClass> component =
        Component.builder(TestClass.class, TestInterface.class).factory(nullFactory).build();
    assertThat(component.getProvidedInterfaces())
        .containsExactly(TestClass.class, TestInterface.class);
  }

  @Test
  public void build_withNoFactoryProvided_shouldThrow() {
    Component.Builder<TestClass> builder = Component.builder(TestClass.class);

    try {
      builder.build();
      fail("Expected exception not thrown.");
    } catch (IllegalStateException e) {
      // success.
    }
  }

  @Test
  public void getFactory_shouldReturnFactorySetInBuilder() {
    Component<TestClass> component =
        Component.builder(TestClass.class).factory(nullFactory).build();
    assertThat(component.getFactory()).isSameAs(nullFactory);
  }
}