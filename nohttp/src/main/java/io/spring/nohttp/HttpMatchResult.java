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

package io.spring.nohttp;

/**
 * Represents where an http:// result which includes the http:// URL that was found and the index (starting at 0) where the result was found.
 * @author Rob Winch
 */
public class HttpMatchResult {
	private final String httpUrl;

	private final int start;

	/**
	 * Creates a new instance
	 * @param httpUrl the http:// URL that was found
	 * @param start the index (starting at 0) where the http:// URL was found
	 */
	public HttpMatchResult(String httpUrl, int start) {
		this.httpUrl = httpUrl;
		this.start = start;
	}

	/**
	 * The http:// URL that was found
	 * @return The http:// URL that was found
	 */
	public String getHttpUrl() {
		return this.httpUrl;
	}

	/**
	 * Gets the index (starting at 0) where the http:// URL was found
	 * @return Gets the index (starting at 0) where the http:// URL was found
	 */
	public int getStart() {
		return this.start;
	}
}
