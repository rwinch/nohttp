/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.nohttp.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.resources.TextResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Rob Winch
 */
public class NoHttpCheckstylePlugin implements Plugin<Project> {
	public static final String CHECKSTYLE_CONFIGURATION_NAME = "checkstyle";

	public static final String DEFAULT_CONFIGURATION_NAME = "nohttp";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private Project project;

	private NoHttpExtension extension;

	@Override
	public void apply(Project project) {
		this.project = project;
		this.extension = this.project.getExtensions().create(DEFAULT_CONFIGURATION_NAME, NoHttpExtension.class);
		this.extension.setToolVersion("0.0.1.BUILD-SNAPSHOT");
		this.extension.setSource(project.fileTree(project.getProjectDir(), new Action<ConfigurableFileTree>() {
			@Override
			public void execute(ConfigurableFileTree files) {
				files.exclude("**/build/**");
				files.exclude(".git/**");
				files.exclude(".gradle/**");
				files.exclude(".idea/**");
				files.exclude("**/*.class");
				files.exclude("**/*.jks");
			}
		}));
		File defaultWhiteListFile = project.file("etc/nohttp/whitelist.lines");
		if (defaultWhiteListFile.exists()) {
			this.extension.setWhitelistsFile(defaultWhiteListFile);
		}

		project.getPluginManager().apply(CheckstylePlugin.class);
		Configuration checkstyleConfiguration = project.getConfigurations().getByName(CHECKSTYLE_CONFIGURATION_NAME);

		Configuration noHttpConfiguration = project.getConfigurations().create(DEFAULT_CONFIGURATION_NAME);
		checkstyleConfiguration.extendsFrom(noHttpConfiguration);

		configureDefaultDependenciesForProject(noHttpConfiguration);
		createCheckstyleTaskForProject(checkstyleConfiguration);
	}

	private void createCheckstyleTaskForProject(Configuration configuration) {
		Project project = this.project;
		Checkstyle checkstyleTask = project
				.getTasks().create("nohttpCheckstyle", Checkstyle.class);

		ConventionMapping taskMapping = checkstyleTask.getConventionMapping();
		taskMapping.map("classpath", new Callable<FileCollection>() {
			@Override
			public FileCollection call() throws Exception {
				return configuration;
			}
		});
		taskMapping.map("source", new Callable<FileTree>() {
			@Override
			public FileTree call() throws Exception {
				return NoHttpCheckstylePlugin.this.extension.getSource();
			}
		});
		taskMapping.map("configProperties", new Callable<Map<String, Object>>() {
			@Override
			public Map<String, Object> call() throws Exception {
				Map<String, Object> configProperties = new HashMap<>();
				File defaultWhiteListFile = project.file("etc/nohttp/whitelist.lines");
				if (defaultWhiteListFile.exists()) {
					logger.debug("Using whitelist at {}", defaultWhiteListFile);
					configProperties.put("nohttp.checkstyle.whitelistFileName", defaultWhiteListFile);
				}
				return configProperties;
			}
		});
		taskMapping.map("config", new Callable<TextResource>() {
			@Override
			public TextResource call() throws Exception {
				File defaultCheckstyleFile = project.file("etc/nohttp/checkstyle.xml");
				if (defaultCheckstyleFile.exists()) {
					logger.debug("Found default checkstyle configuration, so configuring checkstyleTask to use it");
					return project.getResources().getText().fromFile(defaultCheckstyleFile);
				}
				logger.debug("No checkstyle configuration provided, so using the default.");
				URL resource = getClass().getResource(
						"/io/spring/nohttp/checkstyle/default-nohttp-checkstyle.xml");
				return project.getResources().getText().fromUri(resource);
			}
		});
	}

	private void configureDefaultDependenciesForProject(Configuration configuration) {
		configuration.defaultDependencies(new Action<DependencySet>() {
			@Override
			public void execute(DependencySet dependencies) {
				NoHttpExtension extension = NoHttpCheckstylePlugin.this.extension;
				dependencies.add(NoHttpCheckstylePlugin.this.project.getDependencies().create("io.spring.nohttp:nohttp-checkstyle:"  + extension.getToolVersion()));
			}
		});
	}
}
