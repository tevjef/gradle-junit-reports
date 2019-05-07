/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.palantir.gradle.junit

import java.util.LinkedHashMap

import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Task
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class StyleTaskTimer : TaskTimer {

  private val taskTimeNanosByTask = LinkedHashMap<Task, Long>()
  private var lastStartTime: Long = 0

  override fun getTaskTimeNanos(styleTask: Task): Long {
    if (!isStyleTask(styleTask)) {
      throw ClassCastException("not a style task")
    }
    return taskTimeNanosByTask[styleTask] ?: throw IllegalArgumentException("no time available for task")
  }

  override fun beforeExecute(task: Task) {
    if (isStyleTask(task)) {
      lastStartTime = System.nanoTime()
    }
  }

  override fun afterExecute(task: Task, taskState: TaskState) {
    if (isStyleTask(task)) {
      taskTimeNanosByTask[task] = System.nanoTime() - lastStartTime
    }
  }

  companion object {
    fun isStyleTask(task: Task): Boolean {
      return task is Checkstyle ||
          task is FindBugs ||
          task is JavaCompile ||
          task is Detekt ||
          KtlintUtils.isKtLintTask(task) ||
          task is KotlinCompile ||
          AndroidLintUtils.isAndroidLintTask(task)
    }
  }
}
