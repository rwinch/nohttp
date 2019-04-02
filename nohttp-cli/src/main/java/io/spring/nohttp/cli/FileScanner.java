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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Rob Winch
 */
class FileScanner {
	private final File dir;

	private List<Predicate<File>> excludeDirs = new ArrayList<>();

	private List<Predicate<File>> excludeFiles = new ArrayList<>();

	private FileScanner(File dir) {
		this.dir = dir;
	}

	static FileScanner create(File dir) {
		return new FileScanner(dir);
	}

	FileScanner textFiles(boolean textFilesOnly) {
		if (textFilesOnly) {
			return excludeFile(f -> !isTextFile(f));
		} else {
			return this;
		}
	}

	private static boolean isTextFile(File file) {
		try {
			return new ProcessBuilder("grep", "-Iq", ".", file.getAbsolutePath())
					.directory(file.getParentFile())
					.start()
					.waitFor() == 0;
		} catch (Exception e) {
			throw new RuntimeException("Could not determine if " + file + " is a text file. Try explicitly providing the dir or files you want to process", e);
		}
	}

	FileScanner excludeDir(Predicate<File> dirExclusion) {
		this.excludeDirs.add(dirExclusion);
		return this;
	}

	FileScanner excludeFile(Predicate<File> fileExclusion) {
		this.excludeFiles.add(fileExclusion);
		return this;
	}

	void scan(Consumer<File> file) {
		FileScannerVisitor visitor = new FileScannerVisitor(file);
		try {
			Files.walkFileTree(this.dir.toPath(), visitor);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	class FileScannerVisitor extends SimpleFileVisitor<Path> {
		private final Consumer<File> file;

		FileScannerVisitor(Consumer<File> file) {
			this.file = file;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			if (FileScanner.this.excludeDirs.stream().anyMatch(e -> e.test(dir.toFile()))) {
				return FileVisitResult.SKIP_SUBTREE;
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			if (!FileScanner.this.excludeFiles.stream().anyMatch(e -> e.test(file.toFile()))) {
				return FileVisitResult.CONTINUE;
			}
			return FileVisitResult.CONTINUE;
		}
	}
}
