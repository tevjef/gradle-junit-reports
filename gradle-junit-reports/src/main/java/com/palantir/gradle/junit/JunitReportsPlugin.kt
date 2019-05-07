/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.gradle.junit

import com.google.common.base.Splitter
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

class JunitReportsPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    if (project !== project.rootProject) {
      project.logger.warn(
          "com.palantir.junit-reports should be applied to the root project only, not '{}'",
          project.name)
    }

    val reportsExtension = project.extensions
        .create(EXT_JUNIT_REPORTS, JunitReportsExtension::class.java, project)

    configureBuildFailureFinalizer(project.rootProject, reportsExtension.reportsDirectory)

    val timer = StyleTaskTimer()
    project.rootProject.gradle.addListener(timer)

    project.rootProject.allprojects { proj ->
      proj.tasks.withType(Test::class.java) { test ->
        test.reports.junitXml.isEnabled = true
        test.reports.junitXml.setDestination(
            junitPath(reportsExtension.reportsDirectory, test.path))
      }

      proj.tasks.withType(Checkstyle::class.java) { checkstyle ->
        JunitReportsFinalizer.registerFinalizer(
            checkstyle,
            timer,
            XmlReportFailuresSupplier.create(checkstyle, CheckstyleReportHandler()),
            reportsExtension.reportsDirectory.map { dir -> dir.dir("checkstyle") })
      }

      proj.tasks.withType(JavaCompile::class.java) { javac ->
        JunitReportsFinalizer.registerFinalizer(
            javac,
            timer,
            JavacFailuresSupplier.create(javac),
            reportsExtension.reportsDirectory.map { dir -> dir.dir("javac") })
      }

      proj.tasks.withType(KotlinCompile::class.java) { kotlinc ->
        JunitReportsFinalizer.registerFinalizer(
            kotlinc,
            timer,
            KotlinFailuresSupplier.create(kotlinc),
            reportsExtension.reportsDirectory.map { dir -> dir.dir("kotlinc") })
      }

      proj.tasks.withType(Detekt::class.java) { detektTask ->
        JunitReportsFinalizer.registerFinalizer(
            detektTask,
            timer,
            DetektReportFailuresSupplier.create(detektTask, DetektReportHandler()),
            reportsExtension.reportsDirectory.map { dir -> dir.dir("detekt") })
      }

      // Configure default ktlint installation: https://github.com/pinterest/ktlint#-with-gradle
      // TODO Configure for ktlint gradle plugins
      // https://github.com/jeremymailen/kotlinter-gradle
      // https://github.com/jlleitschuh/ktlint-gradle
      proj.tasks.withType(JavaExec::class.java) { javaExecTask ->
        if (!KtlintUtils.isKtLintTask(javaExecTask)) {
          return@withType
        }

        JunitReportsFinalizer.registerFinalizer(
            javaExecTask,
            timer,
            KtlintReportFailuresSupplier.create(javaExecTask, KtlintReportHandler()),
            reportsExtension.reportsDirectory.map { dir -> dir.dir("ktlint") })
      }

      proj.tasks.matching { AndroidLintUtils.isAndroidLintTask(it) }.whenTaskAdded { task ->
        JunitReportsFinalizer.registerFinalizer(
            task,
            timer,
            AndroidLintFailuresSupplier.create(task, null),
            reportsExtension.reportsDirectory.map { dir -> dir.dir("android-lint") })
      }
    }
  }

  companion object {
    val EXT_JUNIT_REPORTS = "junitReports"

    private fun junitPath(basePath: Provider<Directory>, testPath: String): Provider<File> {
      return basePath
          .map { dir -> dir.dir("junit") }
          .map { dir -> dir.file(Splitter.on(':').splitToList(testPath.substring(1)).joinToString(File.separator)) }
          .map { it.asFile }
    }

    private fun configureBuildFailureFinalizer(rootProject: Project, reportsDir: Provider<Directory>) {
      val targetFileProvider = reportsDir.map { dir ->
        var attemptNumber = 1
        var targetFile = dir.asFile.toPath().resolve("gradle").resolve("build.xml")
        while (targetFile.toFile().exists()) {
          targetFile = dir.asFile.toPath().resolve("gradle").resolve("build" + ++attemptNumber + ".xml")
        }
        dir.file(targetFile.toAbsolutePath().toString())
      }

      val listener = BuildFailureListener()
      val action = BuildFinishedAction(targetFileProvider, listener)
      rootProject.gradle.addListener(listener)
      rootProject.gradle.buildFinished(action)
    }
  }
}
