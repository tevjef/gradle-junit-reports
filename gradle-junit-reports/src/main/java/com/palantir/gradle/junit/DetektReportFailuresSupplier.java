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
package com.palantir.gradle.junit;

import io.gitlab.arturbosch.detekt.Detekt;
import io.gitlab.arturbosch.detekt.extensions.DetektReport;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.SingleFileReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public final class DetektReportFailuresSupplier implements FailuresSupplier {

    public static DetektReportFailuresSupplier create(final Detekt task, final ReportHandler<Detekt> reportHandler) {
        // Ensure any necessary output is enabled
        task.doFirst(ignored -> reportHandler.configureTask(task));
        return new DetektReportFailuresSupplier(task, reportHandler);
    }

    private final Detekt reporting;
    private final ReportHandler<?> reportHandler;

    private DetektReportFailuresSupplier(
            Detekt reporting,
            ReportHandler<?> reportHandler) {
        this.reporting = reporting;
        this.reportHandler = reportHandler;
    }

    @Override
    public List<Failure> getFailures() throws IOException {
        File sourceReport = reporting.getXmlReportFile().getOrNull().getAsFile();
        return XmlUtils.parseXml(reportHandler, new FileInputStream(sourceReport)).failures();
    }

    @Override
    public RuntimeException handleInternalFailure(Path reportDir, RuntimeException ex) {
        File report = reporting.getXmlReportFile().getOrNull().getAsFile();
        return new RuntimeException(
                "Finalizer failed; raw report files can be found at " + report.toString(), ex);
    }
}
