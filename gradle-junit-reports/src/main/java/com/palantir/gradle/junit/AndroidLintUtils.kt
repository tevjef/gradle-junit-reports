package com.palantir.gradle.junit

import org.gradle.api.Task

object AndroidLintUtils {

  fun isAndroidLintTask(task: Task): Boolean {
    return task.name.startsWith("lint") && !task.name.endsWith("CircleFinalizer")
  }
}