/*
 * Copyright 2022 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.snyk.gradle

import com.wooga.snyk.GradleInit
import nebula.test.ProjectSpec
import spock.lang.Unroll
import wooga.gradle.snyk.SnykPlugin
import wooga.gradle.snyk.SnykRootPluginExtension
import wooga.gradle.snyk.cli.BusinessCriticalityOption
import wooga.gradle.snyk.cli.EnvironmentOption
import wooga.gradle.snyk.cli.FailOnOption
import wooga.gradle.snyk.cli.LifecycleOption
import wooga.gradle.snyk.cli.SeverityThresholdOption

class GradlePluginSnykConventionPluginSpec extends ProjectSpec {

    public static final String SNYK_PLUGIN_ID = "net.wooga.snyk"
    public static final String PLUGIN_ID = 'net.wooga.snyk-gradle-plugin'

    def "does not die if the snyk plugin is not applied"() {
        given:
        assert !project.plugins.hasPlugin(SNYK_PLUGIN_ID)

        when:
        def conventions = project.plugins.apply(PLUGIN_ID)

        then:
        conventions != null
    }

    @Unroll
    def "applies the right proper conventions for java wdks when the conventions are applied #message the snyk plugin is applied"() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_ID)

        when:
        if (applyAfter) {
            project.plugins.apply(SNYK_PLUGIN_ID)
            project.plugins.apply(PLUGIN_ID)
        } else {
            project.plugins.apply(PLUGIN_ID)
            project.plugins.apply(SNYK_PLUGIN_ID)
        }

        and:
        SnykRootPluginExtension snykExtension = project.extensions.getByName(SnykPlugin.EXTENSION_NAME) as SnykRootPluginExtension

        then:
        snykExtension.projectName.get() == project.name
        snykExtension.projectLifecycle.get() == [LifecycleOption.development, LifecycleOption.production]
        snykExtension.projectEnvironment.get() == [EnvironmentOption.internal]
        snykExtension.projectBusinessCriticality.get() == [BusinessCriticalityOption.medium]
        snykExtension.projectTags.get() == ["team": "atlas", "component": "plugin", "platform": "gradle", "language": "groovy"]
        snykExtension.initScript.get().asFile.text == snykInitScript
        snykExtension.autoDownload.get()
        snykExtension.strategies.get() == [SnykPlugin.MONITOR_CHECK]
        snykExtension.failOn.get() == FailOnOption.all
        snykExtension.severityThreshold.get() == SeverityThresholdOption.high
        snykExtension.orgName.get() == "wooga"

        where:
        applyAfter | message
        true       | "after"
        false      | "before"
    }

    static String getSnykInitScript() {
        GradleInit.snykInitScript.text
    }
}
