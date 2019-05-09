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
import org.xml.sax.Attributes
import java.io.File
import java.util.*

class AndroidLintReportHandler : ReportHandler<Task>() {

  private val failures = ArrayList<Failure>()
  private var file: File? = null

  private var failure: Failure.Builder? = null
  private var line = 0

  override fun configureTask(task: Task) {
  }

  override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes?) {
    when (qName) {
      "issue" -> {
        val id = attributes!!.getValue("id")
        val severity = attributes.getValue("severity")
        val message = attributes.getValue("message")
        val category = attributes.getValue("category")
        val priority = attributes.getValue("priority")
        val summary = attributes.getValue("summary")
        val explanation = attributes.getValue("explanation")
        val url = attributes.getValue("url")
        val urls = attributes.getValue("urls")
        val errorLine1 = attributes.getValue("errorLine1")
        val errorLine2 = attributes.getValue("errorLine2")

        failure = Failure.Builder()
            .severity(severity)
            .source(id)
            .details(message + "\n\n" + explanation)
            .message("[$id] $summary")
      }

      "location" -> {
        file = File(attributes!!.getValue("file"))
        line = attributes.getValue("line").toIntOrNull() ?: 0
        val column = attributes.getValue("column").toIntOrNull() ?: 0
        failure?.line(line)
            ?.column(column)
            ?.file(file)
      }
    }
  }

  override fun endElement(uri: String?, localName: String?, qName: String?) {
    when (qName) {
      "issue" -> {
        failures.add(failure!!.build())
        failure = null
      }
      "location" -> {
        file = null
        line = 0
      }
    }
  }

  override fun failures(): List<Failure> {
    return failures
  }
}
