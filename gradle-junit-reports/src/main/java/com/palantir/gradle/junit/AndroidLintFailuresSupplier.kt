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

import org.gradle.api.Task
import java.io.IOException
import java.nio.file.Path

class AndroidLintFailuresSupplier private constructor(
    private val reporting: Task,
    private val reportHandler: ReportHandler<Task>?) : FailuresSupplier {

  @Throws(IOException::class)
  override fun getFailures(): List<Failure> {
    reporting.project.buildDir.toPath().resolve("reports").toFile().listFiles()
        .filter { it.name.startsWith("lint-results") && it.name.endsWith("xml") }
        .forEach { println(it) }

    return emptyList()
//    return XmlUtils.parseXml(reportHandler, FileInputStream(sourceReport)).failures()
  }

  override fun handleInternalFailure(reportDir: Path, ex: RuntimeException): RuntimeException {
//    val report = reporting.xmlReportFile.orNull!!.asFile
    return ex
  }

  companion object {

    fun create(task: Task, reportHandler: ReportHandler<Task>?): AndroidLintFailuresSupplier {
      return AndroidLintFailuresSupplier(task, reportHandler)
    }
  }
}
