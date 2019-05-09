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

import java.io.File
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

object FailuresReportGenerator {

  private val JAVA_FILE_RX = Pattern.compile(".*src/\\w+/java/(.*)\\.java")

  @JvmStatic
  fun failuresReport(
      rootDir: File,
      projectName: String,
      taskName: String,
      elapsedTimeNanos: Long,
      failures: List<Failure>): Report {
    val report = Report.Builder()
        .elapsedTimeNanos(elapsedTimeNanos)
        .name(projectName)
        .subname(taskName)

    for (failure in failures) {
      val shortSource = if (failure.source().isEmpty()) "" else failure.source().replace(".*\\.".toRegex(), "") + " - "
      val className = getClassName(failure.file())
      val lineColumn = ":${failure.line()}:${failure.column()}"
      val testCase = Report.TestCase.Builder()
          .name(shortSource + className + lineColumn)
          .failure(Report.Failure.Builder()
              .message(failure.file().name + ":" + failure.line() + ":" + failure.column() + " " + failure.message())
              .details(
                  failure.severity() + ": " + failure.message() + failure.details() + "\n\n"
                      + (if (failure.source().isEmpty()) "" else "Category: " + failure.source() + "\n")
                      + "File: " + relativise(rootDir, failure) + "\n"
                      + "Line: " + failure.line() + "\n")
              .build())
          .build()
      report.addTestCases(testCase)
    }

    return report.build()
  }

  fun relativise(rootDir: File, failure: Failure): Path {
    try {
      return rootDir.toPath().relativize(failure.file().toPath())
    } catch (e: IllegalArgumentException) {
      throw IllegalStateException("Could not relativise " + failure.file() + " wrt " + rootDir, e)
    }

  }

  private fun getClassName(file: File): String {
    val matcher = JAVA_FILE_RX.matcher(file.toString())
    return if (matcher.matches()) {
      matcher.group(1).replace('/', '.')
    } else file.toString()
  }

}
