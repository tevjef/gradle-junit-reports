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

import io.gitlab.arturbosch.detekt.Detekt
import org.xml.sax.Attributes

import java.io.File
import java.util.ArrayList

class DetektReportHandler : ReportHandler<Detekt>() {

  private val failures = ArrayList<Failure>()
  private var file: File? = null

  override fun configureTask(task: Detekt) {
    // Ensure XML output is enabled
    task.reports.xml.enabled = true
  }

  override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes?) {
    when (qName) {
      "file" -> file = File(attributes!!.getValue("name"))

      "error" -> failures.add(Failure.Builder()
          .source(attributes!!.getValue("source"))
          .severity(attributes.getValue("severity").toUpperCase())
          .file(file!!)
          .line(Integer.parseInt(attributes.getValue("line")))
          .message(attributes.getValue("message"))
          .build())

      else -> {
      }
    }
  }

  override fun failures(): List<Failure> {
    return failures
  }
}
