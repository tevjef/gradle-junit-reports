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


import com.palantir.gradle.junit.KtlintUtils.KTLINT_REPORT
import org.gradle.api.tasks.JavaExec
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path

class KtlintReportFailuresSupplier private constructor(
    private val reporting: JavaExec,
    private val reportHandler: ReportHandler<JavaExec>) : FailuresSupplier {

  @Throws(IOException::class)
  override fun getFailures(): List<Failure> {
    var report = File(KtlintUtils.getReportDir(reporting.project))

    if (!report.exists()) {
      report = File(KtlintUtils.getOutputDir(reporting))
    }

    if (reporting.outputs.hasOutput) {
      report = reporting.outputs.files.filter { it.name.endsWith("xml") }.singleFile
    }

    return XmlUtils.parseXml(reportHandler, FileInputStream(report)).failures()
  }

  override fun handleInternalFailure(reportDir: Path, ex: RuntimeException): RuntimeException {
    val report = reporting.outputs.files.filter { it.name.endsWith("xml") }.singleFile
    return RuntimeException("Finalizer failed; raw report files can be found at $report", ex)
  }

  companion object {
    fun create(task: JavaExec, reportHandler: ReportHandler<JavaExec>): KtlintReportFailuresSupplier {
      // Ensure any necessary output is enabled
      task.doFirst { reportHandler.configureTask(task) }
      return KtlintReportFailuresSupplier(task, reportHandler)
    }
  }
}
