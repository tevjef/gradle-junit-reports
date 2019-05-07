package com.palantir.gradle.junit

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import java.io.File

object KtlintUtils {

  const val KTLINT_REPORT = "reports/ktlint/ktlint.xml"

  fun getReportDir(project: Project): String {
    return "${project.buildDir}${File.separator}$KTLINT_REPORT"
  }

  fun getOutputDir(task: JavaExec): String? {
    if (!isKtLintTask(task)) {
      return null
    }

    return task.args.orEmpty().find {
      it.contains("--reporter=checkstyle,output")
    }?.split("output=")?.last()
  }

  fun isKtLintTask(task: Task): Boolean {
    if (task !is JavaExec) {
      return false
    }
    return (task.main == "com.pinterest.ktlint.Main" ||
        task.main == "com.github.shyiko.ktlint.Main") &&
        !task.args.orEmpty().contains("-F") ||
        !task.args.orEmpty().contains("--format")
  }
}
