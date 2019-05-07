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


import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.regex.Pattern

class KotlinFailuresSupplier private constructor(
    private val errorStream: StringBuilder) : FailuresSupplier {

  @Throws(IOException::class)
  override fun getFailures(): List<Failure> {
    return errorStream.lines()
        .mapNotNull {
          val matcher = ERROR_LINE.matcher(it)

          if (!matcher.matches()) {
            null
          } else {
            val file = File(matcher.group(2))
            val line = matcher.group(3).toInt()
            val message = matcher.group(4)
            Failure.Builder()
                .file(file)
                .line(line)
                .severity("error")
                .message(message)
                .build()
          }
        }
  }

  override fun handleInternalFailure(reportDir: Path, ex: RuntimeException): RuntimeException {
    return ex
  }

  companion object {
    private val ERROR_LINE = Pattern.compile("(e:) (.*kt): \\((\\d*).*\\): (.*)")

    fun create(task: KotlinCompile): KotlinFailuresSupplier {
      val errorStream = StringBuilder()
      task.logging.addStandardErrorListener { errorStream.append(it) }
      return KotlinFailuresSupplier(errorStream)
    }
  }
}
