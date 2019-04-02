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

package io.spring.nohttp.cli;

import io.spring.nohttp.HttpReplaceResult;
import io.spring.nohttp.HttpReplacer;
import io.spring.nohttp.RegexHttpMatcher;
import io.spring.nohttp.RegexPredicate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author Rob Winch
 */
@CommandLine.Command
@Component
public class ReplaceFilesRunner implements CommandLineRunner, Callable<Void> {
	private InputStream whitelistExclusions;

	@CommandLine.Option(names = "-T", description = "Turns off the text searching.", defaultValue = "true")
	private boolean textFilesOnly = true;

	@CommandLine.Parameters(index = "0", description = "The directory to scan. Default is current working directory.")
	private File dir = new File(System.getProperty("user.dir"));

	@CommandLine.Option(names = "-D")
	private List<Pattern> dirExclusions = new ArrayList<>();

	@CommandLine.Option(names = "-D")
	private List<Pattern> fileExclusions = new ArrayList<>();

	@CommandLine.Option(names = "-w", description = "The path to file that contains additional whitelist of allowed URLs")
	public void setWhitelistFile(File whitelistFile) throws FileNotFoundException {
		this.whitelistExclusions = new FileInputStream(whitelistFile);
	}

	@Override
	public void run(String... args) throws Exception {
		CommandLine.call(this, args);
	}

	@Override
	public Void call() throws Exception {
		RegexHttpMatcher matcher = new RegexHttpMatcher();
		if (this.whitelistExclusions != null) {
			matcher.addHttpUrlWhitelist(RegexPredicate.createWhitelist(this.whitelistExclusions));
		}

		FileScanner.create(this.dir)
			.textFiles(this.textFilesOnly)
			.excludeDir(dirExclusions())
			.excludeFile(fileExclusions())
			.scan(f -> replaceHttpInFile(matcher, f));

		return null;
	}

	private Predicate<File> dirExclusions() {
		return f -> this.dirExclusions.stream().anyMatch(pattern -> pattern.asPredicate().test(f.getName()));
	}

	private Predicate<File> fileExclusions() {
		return f -> this.fileExclusions.stream().anyMatch(pattern -> pattern.asPredicate().test(f.getName()));
	}

	public HttpReplaceResult replaceHttpInFile(HttpReplacer replacer, File file) {
		System.out.println("Processing " + file);
//		String originalText = IOUtils.readText(file);
//
//		HttpReplaceResult result = replacer.replaceHttp(originalText);
//		if (!result.isReplacement()) {
//			return result;
//		}
//
//		IOUtils.writeTextTo(result.getResult(), file);
//		return result;
		return null;
	}
//
//	private static void writeReport(List<HttpMatchResult> results) {
//		Set<String> uniqueHttpUrls = results.stream()
//				.map(HttpMatchResult::getHttpUrl)
//				.collect(Collectors.toSet());
//		List<String> httpUrls = new ArrayList<>(uniqueHttpUrls.size());
//		httpUrls.addAll(uniqueHttpUrls);
//		Collections.sort(httpUrls);
//		System.out.println("");
//		System.out.println("The Following URLs were replaced");
//		System.out.println("");
//		for (String httpUrl : httpUrls) {
//			System.out.println("Replaced " + httpUrl);
//		}
//	}
}
